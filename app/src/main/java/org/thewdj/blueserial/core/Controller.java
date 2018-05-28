package org.thewdj.blueserial.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.ApplicationAdapter;

import org.thewdj.blueserial.core.utility.Sprite;
import org.thewdj.blueserial.core.utility.gui.*;

public class Controller extends ApplicationAdapter {

    Sprite E;
    long PublicTime;
    Texture BtnB, BtnF, SliderB, SliderBS, SliderF;
    Button Btn;
    Slider SlrA, SlrB, Swh;
    float TmpA, TmpB;

    @Override
    public void create() {
        E = new Sprite();

        BtnB = new Texture("Button-U.png");
        BtnF = new Texture("Button-P.png");
        SliderB = new Texture("SliderBody.png");
        SliderBS = new Texture("SliderBodySmall.png");
        SliderF = new Texture("SliderPoint.png");

        Btn = new Button(BtnB, BtnF);
        Btn.X = (Gdx.graphics.getWidth() - Btn.Width) / 2;
        Btn.Y = Btn.Height;
        SlrA = new Slider(SliderB, SliderF);
        SlrA.X = 32;
        SlrA.Y = 32;
        SlrA.IsAutoReset = true;
        SlrB = new Slider(SliderB, SliderF);
        SlrB.X = Gdx.graphics.getWidth() - SlrB.Width - 32;
        SlrB.Y = 32;
        SlrB.IsAutoReset = true;
        Swh = new Slider(SliderBS, SliderF);
        Swh.X = (Gdx.graphics.getWidth() - Swh.Width) / 2;
        Swh.Y = Gdx.graphics.getHeight() - 32 - Swh.Height;
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.98F, 0.98f, 0.98F, 1.0F);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        TmpA = SlrA.Value;
        TmpB = SlrB.Value;

        E.begin();
        Btn.Redner(E);
        SlrA.Redner(E);
        SlrB.Redner(E);
        Swh.Redner(E);
        E.end();
        SwitchLock();

        if (PublicTime % 30 == 0 && BlueSerial.connected) {
            if (Btn.IsClicked)
                BlueSerial.instance.send("L300R300;", false);
            else
                BlueSerial.instance.send("L" + (int) SlrA.Value + "R" + (int) SlrB.Value + ";", false);
        }

        PublicTime++;
    }

    private void SwitchLock() {
        if (Swh.Value > Swh.MaxValue / 2) {
            if (TmpA != SlrA.Value)
                SlrB.Value = SlrA.Value;
            else if (TmpB != SlrB.Value)
                SlrA.Value = SlrB.Value;
        } else if (Swh.Value < -Swh.MaxValue / 2) {
            if (TmpA != SlrA.Value)
                SlrB.Value = -SlrA.Value;
            else if (TmpB != SlrB.Value)
                SlrA.Value = -SlrB.Value;
        }
    }

}

