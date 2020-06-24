package com.tuanhav95.example.fragment

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.tuanhav95.example.R
import kotlinx.android.synthetic.main.fragment_top_exo_player.*

class ExoPlayerTopFragment : Fragment(), Player.EventListener {

    companion object {
        const val STREAM_URL = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
    }

    private lateinit var simpleExoPlayer: SimpleExoPlayer

    private lateinit var mediaDataSourceFactory: DataSource.Factory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_top_exo_player, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context)

        mediaDataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "mediaPlayerSample"))

        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse(STREAM_URL))
        simpleExoPlayer.addListener(this)
        simpleExoPlayer.prepare(mediaSource, false, false)
        simpleExoPlayer.playWhenReady = true

        playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        playerView.player = simpleExoPlayer
        playerView.requestFocus()

        aspectRatioFrameLayout.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        aspectRatioFrameLayout.setAspectRatio(16f / 9)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if (playbackState == Player.STATE_ENDED) {
            simpleExoPlayer.playWhenReady = true
        }
    }

    override fun onStop() {
        super.onStop()
        simpleExoPlayer.removeListener(this)
        simpleExoPlayer.release()
    }

}