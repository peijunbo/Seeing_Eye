package com.hust.seeingeye.Utils;


import static com.hust.seeingeye.Utils.AudioUtils.parse2id;

import android.content.Context;
import android.media.MediaPlayer;


import com.hust.seeingeye.R;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
public class AudioPlayer{

    private static AudioPlayer audio_player;
    private HashMap<String, Integer> sound_map;
    private Queue<MediaPlayer> audio_queue;
    private MediaPlayer on_play;

    private AudioPlayer(){

    }

    //获取唯一实例对象
    public static AudioPlayer getInstance(){
        if(audio_player == null) {
            audio_player = new AudioPlayer();
            audio_player.sound_map = new HashMap<>();
            audio_player.audio_queue = new LinkedList<>();
            //利用map存储识别结果及其对应音频
            Field[] fields = R.raw.class.getDeclaredFields();
            String raw_name;
            //加入音频文件
            for (Field field : fields) {
                raw_name = field.getName();
                int id;
                try {
                    id = field.getInt(R.raw.class);
                    audio_player.sound_map.put(raw_name, id);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return audio_player;
    }

    //在播放器中加入一则名为 audio_name(不加文件类型) 的待播放音频
    public void addAudio(Context context, String audio_name){
        Integer id = sound_map.get(audio_name);
        if(id != null) {
            addAudioById(context, id);
        }
    }

    //向播放队列加入序为id的音频
    public void addAudioById(Context context, Integer id){
        MediaPlayer mediaPlayer = MediaPlayer.create(context, id);
        //为队列中的音频加入播放结束监听
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //播放完成后，开始下一段音频
                playAudioBySequence();
            }
        });
        audio_queue.add(mediaPlayer);
    }

    //实现音频的顺序播放
    public void playAudioBySequence(){
        on_play = audio_queue.poll();
        if(on_play != null) {
            on_play.start();//播放第一段音频
        }
    }

    //根据识别结果和距离加入一组音频
    public void addAudioBySet(Context context, String res, Double distance){
        Integer id = sound_map.get(res);
        if(id != null) {
            addAudioById(context, id);
            addAudioById(context, parse2id(distance));
        }
    }

    //停止播放
    public void stopAudio(){
        audio_queue.clear();
        if(on_play!=null){
            on_play.stop();
            on_play = null;
        }
    }

}

/*
public class AudioPlayer{

    private static AudioPlayer audio_player;
    private HashMap<String, Integer> sound_map;
    private Queue<MediaPlayer> audio_queue;

    private AudioPlayer(){

    }

    //获取唯一实例对象
    public static AudioPlayer getInstance(){
        if(audio_player == null) {
            audio_player = new AudioPlayer();
            audio_player.sound_map = new HashMap<>();
            audio_player.audio_queue = new LinkedList<>();
            //利用map存储识别结果及其对应音频
            Field[] fields = R.raw.class.getDeclaredFields();
            String raw_name;
            //加入音频文件
            for (Field field : fields) {
                raw_name = field.getName();
                int id;
                try {
                    id = field.getInt(R.raw.class);
                    audio_player.sound_map.put(raw_name, id);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return audio_player;
    }

    //在播放器中加入一则名为 audio_name(不加文件类型) 的待播放音频
    private void addAudio(Context context, String audio_name){
        Integer id = sound_map.get(audio_name);
        if(id != null) {
            addAudioById(context, id);
        }
    }

    //向播放队列加入序为id的音频
    public void addAudioById(Context context, Integer id){
        MediaPlayer mediaPlayer = MediaPlayer.create(context, id);
        //为队列中的音频加入播放结束监听
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d("AudioPlayer", "onCompletion: " + mp.toString());
                if (!audio_queue.isEmpty()) {
                    //如果队列非空，播放完成后，开始下一段音频
                    audio_queue.poll().start();
                }
            }
        });
        audio_queue.add(mediaPlayer);
    }

    //实现音频的顺序播放
    public void playAudioBySequence(){
        if(!audio_queue.isEmpty()) {
            audio_queue.poll().start();//播放第一段音频
        }
    }

    //根据识别结果和距离加入一组音频
    public void addAudioBySet(Context context, String res, Double distance){
        Integer id = sound_map.get(res);
        if(id != null) {
            addAudioById(context, id);
            addAudioById(context, AudioUtils.parse2id(distance));
        }
    }

    //停止播放
    public void stopAudio(){
        audio_queue.clear();
    }

}
*/
