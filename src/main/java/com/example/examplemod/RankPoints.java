package com.example.examplemod;

public class RankPoints {

    public static final long KILL_POINTS = 1;
    public static final long BOSS_POINTS = 200;
    public static final long ENCHANT_POINTS_PER_LEVEL = 50;
    public static final long F_BASE_POINTS = -100;
    public static final long MIN_POINTS = F_BASE_POINTS;

    private static final double FIRST_COST = 500;
    private static final double GROWTH = 1.6;
    private static final int MAX_RANK = 200;

    /** Очки, нужные для шага с ранга rank на rank+1 */
    public static long cost(int rank) {
        return Math.round(FIRST_COST * Math.pow(GROWTH, Math.max(0, Math.min(rank, 40))));
    }

    /** Сколько очков нужно, чтобы иметь ранг rank (F = -1) */
    public static long threshold(int rank) {
        if (rank < 0) return F_BASE_POINTS;
        long sum = 0;
        for (int i = 0; i < rank; i++) sum += cost(i);
        return sum;
    }

    /** Ранг по очкам: -1 это F, 0 это E и так далее */
    public static int rankFor(long points) {
        if (points < 0) return -1;
        int rank = 0;
        long remaining = points;
        while (rank < MAX_RANK) {
            long c = cost(rank);
            if (remaining < c) break;
            remaining -= c;
            rank++;
        }
        return rank;
    }
}