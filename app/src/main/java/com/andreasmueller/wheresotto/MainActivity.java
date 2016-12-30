package com.andreasmueller.wheresotto;

import android.app.Activity;
import android.os.Bundle;
import android.test.PerformanceTestCase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends Activity
                          implements View.OnClickListener
{

    private int m_Points = 0;
    private int m_Round = 1;
    private int m_Timer = 10;


    private void setText( int id, String text ) {

        TextView text_view = (TextView) findViewById( id );

        if (null!=text_view) {

            text_view.setText(text);
        }
    }

    private void update() {

        setText( R.id.points, Integer.toString(m_Points));
        setText( R.id.round, Integer.toString(m_Round));
        setText( R.id.timer, Integer.toString(m_Timer));
    }

    private void initRound() {

        m_Timer = 10;

        ViewGroup container = (ViewGroup) findViewById( R.id.activity_main);

        container.removeAllViews();



        MainView main_view = new MainView( this, 8*(10+m_Round));

        container.addView( main_view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT );
        container.addView( getLayoutInflater().inflate(R.layout.activity_main,null));

        // update text views...
        update();
    }

    private void newGame() {

        m_Points = 0;
        m_Round = 1;

        initRound();
    }

    private void showStartFragment() {

        ViewGroup container = (ViewGroup) findViewById( R.id.activity_main );

        container.removeAllViews();

        container.addView( getLayoutInflater().inflate(R.layout.fragment_start,null));

        container.findViewById( R.id.start ).setOnClickListener( this );
    }

    private void showGameOverFragment() {

        ViewGroup container = (ViewGroup) findViewById( R.id.activity_main );

        container.addView( getLayoutInflater().inflate(R.layout.fragment_gameover,null));

        container.findViewById( R.id.playagain ).setOnClickListener( this );
    }

    private void startGame() {

        newGame();
    }

    @Override
    public void onClick( View view ) {

        if (R.id.start==view.getId())
        {
            startGame();
        }
        else if (R.id.playagain==view.getId())
        {
            showStartFragment();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showStartFragment();
    }
}
