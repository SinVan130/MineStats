package com.example.examplemod.client;

import com.example.examplemod.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StatusScreen extends Screen {

    private static final int W = 340, H = 232, LW = 200, RW = 112, CARD_H = 196;

    private enum Tab { STATS, TITLES }

    private record Hit(int x, int y, int w, int h, Runnable action) {
        boolean contains(double px, double py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }
    }

    /** Строка описания; null в списке строк означает небольшой пропуск */
    private record Line(FormattedCharSequence text, int color) {}

    private final List<Hit> hits = new ArrayList<>();
    private Tab tab = Tab.STATS;
    private int rankFilter = -1;
    private int listScroll = 0;
    private int detailScroll = 0;
    private String detailId = null;
    private int curX, curY;
    private int panelX, panelY;
    private List<Component> tooltip = null;

    private int dragging = 0; // 1 = ползунок списка, 2 = ползунок описания
    private int listTrackX, listTrackY, listTrackH, listMax;
    private int detTrackX, detTrackY, detTrackH, detMax;

    public StatusScreen() {
        super(Component.translatable("gui." + ExampleMod.MODID + ".status"));
    }

    private static Component tr(String key, Object... args) {
        return Component.translatable("gui." + ExampleMod.MODID + "." + key, args);
    }

    private static void sendCommand(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.connection.sendCommand(command);
    }

    private static Component classComponent(String cls) {
        if (cls.isEmpty()) {
            return Component.translatable("class." + ExampleMod.MODID + ".none")
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x5B6680)));
        }
        return ClassUtil.displayName(cls)
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClassUtil.color(cls))));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Во сколько раз уменьшить окно, чтобы оно влезло в экран */
    private float uiScale() {
        float sx = (width - 8f) / W;
        float sy = (height - 8f) / H;
        return Math.max(0.5f, Math.min(1f, Math.min(sx, sy)));
    }

    private boolean isHover(int x, int y, int w, int h) {
        return curX >= x && curX < x + w && curY >= y && curY < y + h;
    }

    /**
     * Надпись, которая всегда помещается в maxW: сначала сжимается, потом обрезается с «…».
     * align: 0 = x это левый край, 1 = правый край, 2 = центр.
     */
    private void fit(GuiGraphics g, Component text, int x, int y, int maxW, int color, int align) {
        int w = font.width(text);
        float s = w <= maxW ? 1f : Math.max(0.72f, (float) maxW / w);
        boolean cut = w * s > maxW + 0.5f;
        FormattedCharSequence seq;
        int drawW;
        if (cut) {
            int avail = Math.max(0, (int) (maxW / s) - font.width("…"));
            seq = Language.getInstance().getVisualOrder(
                    FormattedText.composite(font.substrByWidth(text, avail), Component.literal("…")));
            drawW = maxW;
        } else {
            seq = text.getVisualOrderText();
            drawW = Math.round(w * s);
        }
        int dx = align == 1 ? x - drawW : align == 2 ? x - drawW / 2 : x;
        var pose = g.pose();
        pose.pushPose();
        pose.translate(dx, y + (1f - s) * 4f, 0f);
        pose.scale(s, s, 1f);
        g.drawString(font, seq, 0, 0, color, false);
        pose.popPose();
        if (cut && isHover(dx, y - 1, drawW, 10)) tooltip = List.of(text);
    }

    private void button(GuiGraphics g, int x, int y, int w, int h, Component label,
                        boolean enabled, boolean selected, Runnable action) {
        boolean hover = enabled && isHover(x, y, w, h);
        int top = !enabled ? 0xFF121629 : hover ? 0xFF33406B : selected ? 0xFF283257 : 0xFF1D2545;
        int bottom = !enabled ? 0xFF0D1020 : hover ? 0xFF222C4F : selected ? 0xFF1B2342 : 0xFF141A33;
        g.fill(x, y, x + w, y + h, selected ? Ui.ACCENT : Ui.BORDER);
        g.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, top, bottom);
        int color = !enabled ? Ui.DIM : selected ? Ui.ACCENT : Ui.TEXT;
        fit(g, label, x + w / 2, y + (h - 8) / 2, w - 6, color, 2);
        if (enabled) hits.add(new Hit(x, y, w, h, action));
    }

    private void drawScrollbar(GuiGraphics g, int x, int y, int h, double ratio, double pos) {
        int thumbH = Math.max(10, (int) (h * Mth.clamp(ratio, 0.05, 1.0)));
        int thumbY = y + (int) ((h - thumbH) * Mth.clamp(pos, 0.0, 1.0));
        g.fill(x, y, x + 3, y + h, 0xFF161B2E);
        g.fill(x, thumbY, x + 3, thumbY + thumbH, Ui.ACCENT);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        hits.clear();
        tooltip = null;
        listMax = 0;
        detMax = 0;

        float s = uiScale();
        curX = (int) (mouseX / s);
        curY = (int) (mouseY / s);
        panelX = (Math.round(width / s) - W) / 2;
        panelY = (Math.round(height / s) - H) / 2;

        var pose = g.pose();
        pose.pushPose();
        pose.scale(s, s, 1f);
        Ui.panel(g, panelX, panelY, W, H);
        drawHeader(g, player, panelX, panelY);
        if (tab == Tab.STATS) drawStats(g, player, panelX, panelY + 30);
        else drawTitles(g, player, panelX, panelY + 30);
        pose.popPose();

        if (tooltip != null) g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
    }

    private void drawHeader(GuiGraphics g, LocalPlayer p, int x, int y) {
        button(g, x + 8, y + 7, 96, 16, tr("tab.stats"), true, tab == Tab.STATS, () -> tab = Tab.STATS);
        button(g, x + 108, y + 7, 70, 16, tr("tab.titles"), true, tab == Tab.TITLES, () -> {
            tab = Tab.TITLES;
            listScroll = 0;
        });
        PlayerStats s = p.getData(ModAttachments.PLAYER_STATS);
        String cls = p.getData(ModAttachments.PLAYER_CLASS);
        fit(g, tr("header_class", classComponent(cls), s.level()), x + W - 10, y + 11,
                W - 190 - 10, Ui.ACCENT, 1);
        g.fill(x + 8, y + 27, x + W - 8, y + 28, Ui.BORDER);
    }

    // ===================== Характеристики =====================

    private void drawStats(GuiGraphics g, LocalPlayer p, int x, int y) {
        PlayerStats s = p.getData(ModAttachments.PLAYER_STATS);
        String cls = p.getData(ModAttachments.PLAYER_CLASS);
        int lx = x + 10;
        int rx = lx + LW + 8;
        Ui.inset(g, lx, y, LW, CARD_H);
        Ui.inset(g, rx, y, RW, CARD_H);

        int cost = s.levelUpCost();
        int have = XpUtil.total(p);

        fit(g, tr("class", classComponent(cls)), lx + 8, y + 6, LW - 16, Ui.MUTED, 0);
        fit(g, tr("level", s.level()), lx + 8, y + 17, 90, Ui.ACCENT, 0);
        fit(g, tr("mana", s.mana(), s.maxMana()), lx + LW - 8, y + 17, 96, Ui.MANA, 1);
        fit(g, tr("next_level", cost, have), lx + 8, y + 28, LW - 16, have >= cost ? Ui.GOOD : Ui.MUTED, 0);
        Ui.bar(g, lx + 8, y + 40, LW - 16, 7, (float) s.mana() / Math.max(1, s.maxMana()), Ui.MANA);

        Stat[] stats = Stat.values();
        for (int i = 0; i < stats.length; i++) {
            Stat st = stats[i];
            int ry = y + 54 + i * 26;
            int val = s.get(st);
            int col = Ui.opaque(st.rgb);

            fit(g, st.displayName(), lx + 8, ry, 100, col, 0);
            String v = String.valueOf(val);
            g.drawString(font, v, lx + 8 + 120 - font.width(v), ry, Ui.TEXT, false);
            Ui.bar(g, lx + 8, ry + 10, 120, 5, val / (float) PlayerStats.MAX_STAT, col);
            fit(g, Component.translatable("stat." + ExampleMod.MODID + "." + st.id + ".effect"),
                    lx + 8, ry + 17, 150, Ui.MUTED, 0);

            boolean max = val >= PlayerStats.MAX_STAT;
            boolean can = !max && have >= cost;
            int bx = lx + LW - 8 - 22;
            int by = ry + 1;
            button(g, bx, by, 22, 20, Component.literal("+"), can, false,
                    () -> sendCommand("stats levelup " + st.id));
            if (isHover(bx, by, 22, 20)) {
                tooltip = List.of(max ? tr("plus.max") : can ? tr("plus.ok", cost) : tr("plus.no_xp", cost));
            }
        }

        // итоги
        double speed = p.getAttributeValue(Attributes.MOVEMENT_SPEED) / 0.1 * 100.0;
        int luck = LuckUtil.getLuck(p);
        double proc = (1.0 - Math.pow(1.0 - LuckUtil.BASE_PROC, luck)) * 100.0;

        fit(g, tr("total"), rx + 8, y + 6, RW - 16, Ui.ACCENT, 0);
        String[] labels = {"health", "damage", "attack_speed", "speed", "mana_regen", "luck", "proc"};
        String[] values = {
                String.format("%.0f", p.getMaxHealth()),
                String.format("%.2f", p.getData(ModAttachments.ATTACK_DAMAGE_SYNC)),
                String.format("%.2f", p.getAttributeValue(Attributes.ATTACK_SPEED)),
                String.format("%.0f%%", speed),
                String.valueOf(s.manaRegen()),
                String.valueOf(luck),
                String.format("%.1f%%", proc)
        };
        for (int i = 0; i < labels.length; i++) {
            int ey = y + 20 + i * 24;
            fit(g, tr("total." + labels[i]), rx + 8, ey, RW - 16, Ui.MUTED, 0);
            fit(g, Component.literal(values[i]), rx + 8, ey + 10, RW - 16, Ui.TEXT, 0);
        }
    }

    // ===================== Титулы =====================

    private List<Line> detailLines(Titles.Def d, boolean has, int wrapWidth) {
        List<Line> out = new ArrayList<>();
        for (FormattedCharSequence l : font.split(d.styledName(), wrapWidth)) out.add(new Line(l, Ui.TEXT));
        Component rarity = Component.translatable("tooltip." + ExampleMod.MODID + ".rarity",
                WeaponRarity.component(d.rank()));
        out.add(new Line(rarity.getVisualOrderText(), Ui.MUTED));
        out.add(null);
        for (FormattedCharSequence l : font.split(d.description(), wrapWidth)) out.add(new Line(l, Ui.TEXT));
        out.add(null);
        if (has) {
            out.add(new Line(tr("titles.unlocked").getVisualOrderText(), Ui.GOOD));
        } else {
            out.add(new Line(tr("titles.locked").getVisualOrderText(), Ui.BAD));
            out.add(new Line(tr("titles.how").getVisualOrderText(), Ui.MUTED));
            for (FormattedCharSequence l : font.split(d.condition().hint(), wrapWidth)) {
                out.add(new Line(l, Ui.MUTED));
            }
        }
        return out;
    }

    private void drawTitles(GuiGraphics g, LocalPlayer p, int x, int y) {
        TitleData data = p.getData(ModAttachments.TITLES);
        Titles.Def active = TitleEvents.selected(p);
        int lx = x + 10;
        int rx = lx + LW + 8;

        button(g, lx, y, 30, 14, tr("titles.all"), true, rankFilter < 0, () -> {
            rankFilter = -1;
            listScroll = 0;
        });
        for (int r = 0; r <= 5; r++) {
            int rank = r;
            button(g, lx + 32 + r * 24, y, 22, 14, WeaponRarity.component(r), true, rankFilter == r, () -> {
                rankFilter = rank;
                listScroll = 0;
            });
        }
        int total = Titles.all().size();
        int have = (int) Titles.all().stream().filter(t -> data.has(t.id())).count();
        fit(g, tr("titles.count", have, total), rx + 8, y + 4, RW - 16, Ui.MUTED, 0);

        List<Titles.Def> list = Titles.all().stream()
                .filter(t -> rankFilter < 0 || t.rank() == rankFilter)
                .sorted(Comparator.<Titles.Def>comparingInt(t -> data.has(t.id()) ? 0 : 1)
                        .thenComparingInt(Titles.Def::rank)
                        .thenComparing(Titles.Def::id))
                .toList();

        if (detailId == null || Titles.get(detailId) == null) {
            detailId = active != null ? active.id() : (list.isEmpty() ? null : list.get(0).id());
            detailScroll = 0;
        }

        int ly = y + 20;
        int lh = CARD_H - 20;
        Ui.inset(g, lx, ly, LW, lh);
        int rows = (lh - 6) / 18;
        int maxScroll = Math.max(0, list.size() - rows);
        listScroll = Mth.clamp(listScroll, 0, maxScroll);

        g.enableScissor(lx + 1, ly + 1, lx + LW - 1, ly + lh - 1);
        for (int i = 0; i < rows && listScroll + i < list.size(); i++) {
            Titles.Def t = list.get(listScroll + i);
            int ry = ly + 3 + i * 18;
            boolean has = data.has(t.id());
            boolean sel = t.id().equals(detailId);
            if (sel) g.fill(lx + 3, ry, lx + LW - 9, ry + 17, 0x40FFC83D);
            else if (isHover(lx + 3, ry, LW - 12, 17)) g.fill(lx + 3, ry, lx + LW - 9, ry + 17, 0x22FFFFFF);

            int rc = Ui.opaque(WeaponRarity.rgb(t.rank()));
            boolean isActive = active != null && active.id().equals(t.id());
            String mark = isActive ? "★" : has ? "●" : "○";
            g.drawString(font, mark, lx + 8, ry + 5, has ? rc : Ui.DIM, false);
            Component rk = WeaponRarity.component(t.rank());
            int rkW = font.width(rk);
            fit(g, t.name(), lx + 20, ry + 5, LW - 20 - 14 - rkW - 6, has ? rc : Ui.DIM, 0);
            g.drawString(font, rk, lx + LW - 14 - rkW, ry + 5, Ui.TEXT, false);

            hits.add(new Hit(lx + 3, ry, LW - 12, 17, () -> {
                detailId = t.id();
                detailScroll = 0;
            }));
        }
        g.disableScissor();

        if (maxScroll > 0) {
            listTrackX = lx + LW - 6;
            listTrackY = ly + 3;
            listTrackH = lh - 6;
            listMax = maxScroll;
            drawScrollbar(g, listTrackX, listTrackY, listTrackH, rows / (double) list.size(),
                    listScroll / (double) maxScroll);
        }

        // описание выбранного титула
        Ui.inset(g, rx, ly, RW, lh);
        Titles.Def d = detailId == null ? null : Titles.get(detailId);
        if (d == null) {
            int cy = ly + 8;
            for (FormattedCharSequence line : font.split(tr("titles.pick"), RW - 16)) {
                g.drawString(font, line, rx + 8, cy, Ui.MUTED, false);
                cy += 10;
            }
            return;
        }

        boolean has = data.has(d.id());
        int viewTop = ly + 4;
        int viewH = lh - 30; // снизу место под кнопку
        List<Line> lines = detailLines(d, has, RW - 20);
        int contentH = 0;
        for (Line l : lines) contentH += l == null ? 5 : 10;
        int maxDetail = Math.max(0, contentH - viewH);
        detailScroll = Mth.clamp(detailScroll, 0, maxDetail);

        g.enableScissor(rx + 1, viewTop, rx + RW - 1, viewTop + viewH);
        int cy = viewTop - detailScroll;
        for (Line l : lines) {
            if (l == null) {
                cy += 5;
                continue;
            }
            g.drawString(font, l.text(), rx + 8, cy, l.color(), false);
            cy += 10;
        }
        g.disableScissor();

        if (maxDetail > 0) {
            detTrackX = rx + RW - 6;
            detTrackY = viewTop;
            detTrackH = viewH;
            detMax = maxDetail;
            drawScrollbar(g, detTrackX, detTrackY, detTrackH, viewH / (double) contentH,
                    detailScroll / (double) maxDetail);
        }

        boolean equipped = active != null && active.id().equals(d.id());
        Component label = equipped ? tr("titles.unequip") : has ? tr("titles.equip") : tr("titles.locked");
        String id = d.id();
        button(g, rx + 8, ly + lh - 22, RW - 16, 16, label, has, equipped,
                () -> sendCommand(equipped ? "titles clear" : "titles set " + id));
    }

    // ===================== Ввод =====================

    private void dragTo(double vy) {
        if (dragging == 1 && listMax > 0) {
            listScroll = (int) Math.round(Mth.clamp((vy - listTrackY) / listTrackH, 0.0, 1.0) * listMax);
        } else if (dragging == 2 && detMax > 0) {
            detailScroll = (int) Math.round(Mth.clamp((vy - detTrackY) / detTrackH, 0.0, 1.0) * detMax);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            float s = uiScale();
            double vx = mx / s;
            double vy = my / s;

            if (listMax > 0 && vx >= listTrackX - 3 && vx <= listTrackX + 6
                    && vy >= listTrackY && vy <= listTrackY + listTrackH) {
                dragging = 1;
                dragTo(vy);
                return true;
            }
            if (detMax > 0 && vx >= detTrackX - 3 && vx <= detTrackX + 6
                    && vy >= detTrackY && vy <= detTrackY + detTrackH) {
                dragging = 2;
                dragTo(vy);
                return true;
            }
            for (int i = hits.size() - 1; i >= 0; i--) {
                Hit h = hits.get(i);
                if (h.contains(vx, vy)) {
                    Minecraft.getInstance().getSoundManager()
                            .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    h.action().run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != 0) {
            dragTo(my / uiScale());
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = 0;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (tab == Tab.TITLES) {
            double vx = mx / uiScale();
            int rx = panelX + 10 + LW + 8;
            int step = (int) Math.signum(scrollY);
            if (vx >= rx && vx < rx + RW) detailScroll = Math.max(0, detailScroll - step * 10);
            else listScroll = Math.max(0, listScroll - step * 2);
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (ClientModEvents.OPEN_STATUS.matches(key, scan)) {
            onClose();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }
}