package com.andreasmueller.wheresotto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import java.util.Random;

/**
 * Created by Andreas on 29.12.2016.
 */

public class MainView extends View {

    private int m_ImageCount = 0;
    private Paint m_Paint = new Paint();
    private Random m_RND;
    private long m_Seed = 0;

    private static final int[] images = {
            R.drawable.decoy_a,
            R.drawable.decoy_b,
            R.drawable.decoy_c
    };

    public MainView(Context context, int imageCount) {
        super(context);

        m_ImageCount = imageCount;
        m_Paint.setAntiAlias( true );

        m_Seed = System.currentTimeMillis();
    }

    @Override
    protected void onDraw( Canvas canvas ) {

        super.onDraw( canvas );

        m_RND = new Random(m_Seed);

        for (int image : images ) {

            Bitmap bitmap = BitmapFactory.decodeResource( getResources(), image );

            for (int i=0; i<m_ImageCount/images.length; ++i) {

                float left = (float) (m_RND.nextFloat() * getWidth() - bitmap.getWidth());
                float top = (float) (m_RND.nextFloat() * getHeight() - bitmap.getHeight());

                canvas.drawBitmap( bitmap, left, top, m_Paint );
            }

            bitmap.recycle();
        }
    }
}
