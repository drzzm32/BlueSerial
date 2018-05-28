package org.thewdj.blueserial.core.utility;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by D.zzm on 2015.6.17.
 */

public class Sprite {

    private SpriteBatch Batch;
    private BitmapFont Font;

    public void begin() {
        Batch.begin();
    }

    public void end() {
        Batch.end();
    }

    public void drawString(float x, float y, float Scale, Color color, String Str) {
        Font.getData().setScale(Scale);
        Font.setColor(color);
        Font.draw(Batch, Str, x, y);
    }

    public void drawBack(Texture Tex) {
        Batch.draw(Tex, 0, 0, Tex.getWidth(), Tex.getHeight());
    }

    public void drawPic(Texture Tex, float x, float y, int Rotate) {
        drawPic(Tex, x, y, Rotate, 1, 1);
    }

    public void drawPic(Texture Tex, float x, float y, float alpha) {
        drawPic(Tex, x, y, 0, 1, 1, 1, 1, 1, alpha);
    }

    public void drawPic(Texture Tex, float x, float y, float Rotate, float ScX, float ScY) {
        drawPic(Tex, x, y, Rotate, ScX, ScY, 1, 1, 1, 1);
    }

    public void drawPic(Texture Tex, float x, float y, float Rotate, float ScX, float ScY, float r, float g, float b, float a) {
        int Width = Tex.getWidth(), Height = Tex.getHeight();
        Batch.setColor(r, g, b, a);
        Batch.draw(Tex, x, y, 0, 0, Width, Height, ScX, ScY, Rotate, 0, 0, Width, Height, false, false);
        Batch.setColor(1, 1, 1, 1);
    }

    public Sprite() {
        Batch = new SpriteBatch();
        Font = new BitmapFont();
    }
}