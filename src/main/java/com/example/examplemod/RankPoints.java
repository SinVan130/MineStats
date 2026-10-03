package com.example.examplemod;

public class RankPoints {

    public static final long KILL_POINTS = 1;
    public static final long BOSS_POINTS = 200;
    public static final long ENCHANT_POINTS_PER_LEVEL = 50;

    private static final double FIRST_COST = 500;
    private static final double GROWTH = 1.6;
    private static final int MAX_RANK = 200;

    /** Очки, нужные для шага с ранга rank на rank+1 */
    public static long cost(int rank) {
        return Math.round(FIRST_COST * Math.pow(GROWTH, Math.min(rank, 40)));
    }

    /** Сколько очков всего нужно, чтобы иметь ранг rank */
    public static long threshold(int rank) {
        long sum = 0;
        for (int i = 0; i < rank; i++) sum += cost(i);
        return sum;
    }

    public static int rankFor(long points) {
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