package io.rolique.kung_fu_karaoke.screen

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.rolique.kung_fu_karaoke.R

/**
 * Created by Victor Artemyev on 10/06/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

class KaraokeActivity : AppCompatActivity() {

    lateinit var mBandWithMeter: BandwidthMeter
    lateinit var mDataSourceFactory: DataSource.Factory

    lateinit var mSimpleExoPlayerView: SimpleExoPlayerView
    lateinit var mPlayer: SimpleExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_karaoke)

        mBandWithMeter = DefaultBandwidthMeter()
        mDataSourceFactory = DefaultDataSourceFactory(
                this@KaraokeActivity, Util.getUserAgent(this@KaraokeActivity, getString(R.string.app_name)))

        mSimpleExoPlayerView = findViewById(R.id.player_view) as SimpleExoPlayerView

    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23) {
            initializePlayer()
        }
    }

    fun initializePlayer() {
        mSimpleExoPlayerView.requestFocus()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(mBandWithMeter)
        mPlayer = ExoPlayerFactory.newSimpleInstance(this@KaraokeActivity, DefaultTrackSelector(videoTrackSelectionFactory))
        mPlayer.playWhenReady = true
        mSimpleExoPlayerView.player = mPlayer
        val videoPath = "asset:///KungFuVideo.mp4"
        val videoUri = Uri.parse(videoPath)
        val mediaSource = ExtractorMediaSource(videoUri, mDataSourceFactory, DefaultExtractorsFactory(), null, null)
        mPlayer.prepare(mediaSource)
    }

    override fun onPause() {
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
        super.onPause()
    }

    override fun onStop() {
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
        super.onStop()
    }

    fun releasePlayer() {
        mPlayer.release()
    }
}
