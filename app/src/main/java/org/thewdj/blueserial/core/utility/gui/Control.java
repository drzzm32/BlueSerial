package org.thewdj.blueserial.core.utility.gui;

import com.badlogic.gdx.graphics.Texture;

import org.thewdj.blueserial.core.utility.*;

/**
 * Created by drzzm on 2016.1.3.
 */
public class Control {
    protected Texture Back, Front;
    public boolean IsEnabled;
    public float X, Y, Width, Height;
    protected float trans;

    public Control() {
    }

    public Control(Texture BackTexture, Texture FrontTexture) {
        Back = BackTexture;
        Front = FrontTexture;
        IsEnabled = true;
        X = 0; Y = 0;
        Width = Back.getWidth(); Height = Back.getHeight();
        trans = 1;
    }

    public Control(Texture BackTexture, Texture FrontTexture, float trans) {
        Back = BackTexture;
        Front = FrontTexture;
        IsEnabled = true;
        X = 0; Y = 0;
        Width = Back.getWidth() * trans; Height = Back.getHeight() * trans;
        this.trans = trans;
    }

    public void Redner(Sprite E) {
        if (IsEnabled) {
            E.drawPic(Back, X, Y, 0);
            RenderFront(E);
        }
    }

    protected void RenderFront(Sprite E) {
    }
}
