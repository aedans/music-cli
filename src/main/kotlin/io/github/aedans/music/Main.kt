package io.github.aedans.music

import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.COMMON_PCM_S16_BE
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val audioPlayerManager = DefaultAudioPlayerManager()
        audioPlayerManager.configuration.outputFormat = COMMON_PCM_S16_BE

        AudioSourceManagers.registerRemoteSources(audioPlayerManager)

        val audioPlayer = audioPlayerManager.createPlayer()

        val loadItem = audioPlayerManager.loadItem("https://soundcloud.com/mrsuicidesheep/the-journey-mix",
            object : AudioLoadResultHandler {
                override fun loadFailed(exception: FriendlyException) {
                    throw exception
                }

                override fun noMatches() {
                    throw Exception("No matches")
                }

                override fun trackLoaded(track: AudioTrack) {
                    audioPlayer.playTrack(track)
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                }
            })

        loadItem.get()

        val audioDataFormat = audioPlayerManager.configuration.outputFormat
        val stream = AudioPlayerInputStream.createStream(audioPlayer, audioDataFormat, 10000L, false)
        val info = DataLine.Info(SourceDataLine::class.java, stream.format)
        val line = AudioSystem.getLine(info) as SourceDataLine
        val buffer = ByteArray(COMMON_PCM_S16_BE.maximumChunkSize())

        line.open(stream.format)
        line.start()

        var chunkSize: Int
        while (run { chunkSize = stream.read(buffer); chunkSize } >= 0) {
            line.write(buffer, 0, chunkSize)
        }
    }
}