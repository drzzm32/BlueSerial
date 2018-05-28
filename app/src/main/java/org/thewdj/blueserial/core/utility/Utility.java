package org.thewdj.blueserial.core.utility;

import com.badlogic.gdx.Gdx;

/**
 * Created by D.zzm on 2015.6.16.
 */
public class Utility {

    public static float Distance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow((double) x2 - (double) x1, (double) 2) + Math.pow((double) y2 - (double) y1, (double) 2));
    }

    public static boolean IsTouched() {
        return Gdx.input.isTouched();
    }

    public static boolean IsTouched(int Pointer) {
        return Gdx.input.isTouched(Pointer);
    }

    public static float GetX() {
        return Gdx.input.getX();
    }

    public static float GetX(int Pointer) {
        return Gdx.input.getX(Pointer);
    }

    public static float GetY() {
        return Gdx.graphics.getHeight() - Gdx.input.getY();
    }

    public static float GetY(int Pointer) {
        return Gdx.graphics.getHeight() - Gdx.input.getY(Pointer);
    }

    public static float GetDX() {
        return Gdx.input.getDeltaX();
    }

    public static float GetDX(int Pointer) {
        return Gdx.input.getDeltaX(Pointer);
    }

    public static float GetDY() {
        return -Gdx.input.getDeltaY();
    }

    public static float GetDY(int Pointer) {
        return -Gdx.input.getDeltaY(Pointer);
    }
}
