package net.supersonic.hud;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

/** Counts mouse clicks over the last second (polled every rendered frame). */
public final class CpsCounter {
    private static final Deque<Long> LEFT = new ArrayDeque<>();
    private static final Deque<Long> RIGHT = new ArrayDeque<>();
    private static boolean leftDown = false;
    private static boolean rightDown = false;

    private CpsCounter() {
    }

    public static void poll(long window) {
        long now = System.currentTimeMillis();

        boolean l = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        if (l && !leftDown) LEFT.addLast(now);
        if (r && !rightDown) RIGHT.addLast(now);
        leftDown = l;
        rightDown = r;

        prune(LEFT, now);
        prune(RIGHT, now);
    }

    private static void prune(Deque<Long> q, long now) {
        while (!q.isEmpty() && now - q.peekFirst() > 1000L) {
            q.pollFirst();
        }
    }

    public static int left() {
        return LEFT.size();
    }

    public static int right() {
        return RIGHT.size();
    }
}
