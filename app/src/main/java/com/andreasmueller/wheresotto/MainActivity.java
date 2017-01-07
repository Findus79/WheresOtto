package com.andreasmueller.wheresotto;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Game;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.internal.IGamesService;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.example.games.basegameutils.GameHelper;

import java.util.Random;


public class MainActivity extends FragmentActivity
        implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks
{
    private int m_Points = 0;
    private int m_Round = 1;
    private int m_Timer = 10;

    private Random m_RND = new Random();

    private ImageView m_Otto;
    private static final int OTTO_ID = 2512354;


    private android.os.Handler m_Handler = new android.os.Handler();
    private Runnable m_Runnable = new Runnable() {
        @Override
        public void run() {
            countdown();
        }
    };


    /* google play api */
    private GoogleApiClient m_GoogleAPIClient;
    // Are we currently resolving a connection failure?
    private boolean m_ResolvingConnectionFailure = false;
    // Has the user clicked the sign-in button?
    private boolean m_SignInClicked = false;
    // Automatically start the sign-in flow when the Activity starts
    private boolean m_AutoStartSignInFlow = true;
    // request codes we use when invoking an external activity
    private static final int RC_RESOLVE = 5000;
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;
    private AdView m_AdView;


    public void countdown() {

        --m_Timer;
        update();

        if (0==m_Timer)
        {
            m_Otto.setOnClickListener( null );
            showGameOverFragment();
        }
        else {
            m_Handler.postDelayed(m_Runnable, 1000);
        }
    }


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

        // add the otto
        m_Otto = new ImageView( this );
        m_Otto.setId( OTTO_ID );
        m_Otto.setImageResource( R.drawable.otto );
        m_Otto.setScaleType(ImageView.ScaleType.CENTER );

        float scale = getResources().getDisplayMetrics().density;

        FrameLayout.LayoutParams layout_params = new FrameLayout.LayoutParams( Math.round(64*scale), Math.round(64*scale), Gravity.TOP + Gravity.LEFT );
        layout_params.leftMargin = m_RND.nextInt( container.getWidth() - 64 );
        layout_params.topMargin = m_RND.nextInt( container.getHeight() - 64 );

        m_Otto.setOnClickListener( this );

        MainView main_view = new MainView( this, 8*(10+m_Round));

        container.addView( m_Otto, layout_params );
        container.addView( main_view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT );
        container.addView( getLayoutInflater().inflate(R.layout.activity_main,null));

        // update text views...
        m_Handler.postDelayed( m_Runnable, 1000 );

        update();
    }

    private void newGame() {

        m_RND.setSeed(SystemClock.currentThreadTimeMillis());
        m_Points = 0;
        m_Round = 1;

        initRound();
    }

    private void showStartFragment() {

        ViewGroup container = (ViewGroup) findViewById( R.id.activity_main );

        container.removeAllViews();

        container.addView( getLayoutInflater().inflate(R.layout.fragment_start,null));

        container.findViewById( R.id.start ).setOnClickListener( this );

        // load banner ad...
        m_AdView = (AdView) findViewById(R.id.adView);
        AdRequest ad_request = new AdRequest.Builder().build();
        m_AdView.loadAd(ad_request);
    }

    private void showGameOverFragment() {

        ViewGroup container = (ViewGroup) findViewById( R.id.activity_main );

        container.addView( getLayoutInflater().inflate(R.layout.fragment_gameover,null));

        container.findViewById( R.id.playagain ).setOnClickListener( this );

        if (m_GoogleAPIClient.isConnected()) {

            Games.Leaderboards.submitScore( m_GoogleAPIClient, getString(R.string.leaderboard_id), m_Points );
        }
    }

    private void startGame() {

        newGame();
    }

    @Override
    public void onStart() {

        super.onStart();

        m_GoogleAPIClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (m_GoogleAPIClient.isConnected())
        {
            m_GoogleAPIClient.disconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        m_Handler.removeCallbacks( m_Runnable );
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
        else if (OTTO_ID==view.getId())
        {
            m_Handler.removeCallbacks( m_Runnable );
            Toast.makeText( this, new String("found him!"), Toast.LENGTH_SHORT).show();

            m_Points += m_Timer * 1000;
            ++m_Round;

            // check achievements depending on timer and round
            if (m_GoogleAPIClient.isConnected()) {

                if (m_Timer >= 9) {
                    Games.Achievements.unlock( m_GoogleAPIClient, getString(R.string.achievement_quick));
                }

                if (m_Timer<=1) {
                    Games.Achievements.unlock(m_GoogleAPIClient, getString(R.string.achievement_close));
                }

                if (m_Round>=50) {
                    Games.Achievements.unlock(m_GoogleAPIClient,getString(R.string.achievement_master));
                }

                if (m_Round>=20) {
                    Games.Achievements.unlock(m_GoogleAPIClient,getString(R.string.achievement_wizard));
                }
            }




            initRound();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init google play stuff...
        m_GoogleAPIClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        showStartFragment();
    }

    private boolean isSignedIn() {

        return (null!=m_GoogleAPIClient && m_GoogleAPIClient.isConnected());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            m_SignInClicked = false;
            m_ResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                m_GoogleAPIClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.signin_other_error);
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

        m_GoogleAPIClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        if (m_ResolvingConnectionFailure) {
            return;
        }

        if (m_SignInClicked || m_AutoStartSignInFlow) {
            m_AutoStartSignInFlow = false;
            m_SignInClicked = false;
            m_ResolvingConnectionFailure = true;
            if (!BaseGameUtils.resolveConnectionFailure(this, m_GoogleAPIClient, connectionResult,
                    RC_SIGN_IN, getString(R.string.signin_other_error))) {
                m_ResolvingConnectionFailure = false;
            }
        }
    }
}
