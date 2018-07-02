package com.acromax.acromaxmediaplayer.view.activity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.acromax.acromaxmediaplayer.R;
import com.acromax.acromaxmediaplayer.model.AudioChunk;
import com.acromax.acromaxmediaplayer.network.AcromaxPlayerService;
import com.acromax.acromaxmediaplayer.network.message.StreamMessage;
import com.acromax.acromaxmediaplayer.util.GlobalData;
import com.acromax.acromaxmediaplayer.util.enums.ApiRequestType;
import com.acromax.acromaxmediaplayer.util.enums.PlayerMode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Created by Mihira on 6/30/2018.
 */
public class PlayerActivity extends BaseActivity implements StreamMessage,  View.OnClickListener, View.OnTouchListener, Runnable {

    private String TAG = PlayerActivity.class.toString();

    private String flagPlayerMode, flagStreamType;
    private Subscription subscription;
    private RelativeLayout relativeLayout;
    private ProgressBar progressBar;
    private ImageButton ib_play;
    private boolean playClicked, pauseClicked;
    private ArrayList<AudioChunk> audioChunkList;
    private int nextChunkArrive = 0;
    private int mediaDuration;
    private int _yDelta;
    private int _xDelta;
    private File audioFile;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Player mode - UNINITIALIZED
        flagPlayerMode = PlayerMode.UNINITIALIZED.toString();

        initializeUI();
    }

    /****************************************/
    /******* Initialise UI Controls *********/
    /****************************************/
    private void initializeUI() {

        Log.d("TAG", TAG);

        relativeLayout = (RelativeLayout) findViewById(R.id.rl_player);
        relativeLayout.setOnTouchListener(this);

        ib_play = (ImageButton) findViewById(R.id.ib_play);
        ib_play.setOnClickListener(this);
        ib_play.setImageResource(R.drawable.ic_play);

        progressBar = findViewById(R.id.progress_bar);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaDuration = mediaPlayer.getDuration();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // player play button click
            case R.id.ib_play:
                if (isConnected(this)) {
                    playAudioFile();
                } else {
                    Toast.makeText(this, "Check Internet Connection", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * Play audio file action
     */
    private void playAudioFile() {

        // check initial click
        if (!playClicked && !pauseClicked) {

            playClicked = true;

            if (flagPlayerMode.equalsIgnoreCase(PlayerMode.UNINITIALIZED.toString())) {
                // Player mode - FETCHING
                flagPlayerMode = PlayerMode.FETCHING.toString();

                this.initialStreamSubscription();

            } else if (flagPlayerMode.equalsIgnoreCase(PlayerMode.PLAYING.toString())) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    pauseClicked = false;
                    playClicked = true;
                    // Player mode - PLAYING
                    flagPlayerMode = PlayerMode.PLAYING.toString();
                    ib_play.setImageResource(R.drawable.ic_pause);
                }
            }

        } else if (pauseClicked) {
            ib_play.setImageResource(R.drawable.ic_pause);

            // Player mode - PLAYING
            flagPlayerMode = PlayerMode.PLAYING.toString();

            mediaPlayer.start();
            pauseClicked = false;
            playClicked = true;

        } else if (playClicked && !pauseClicked) {

            playClicked = true;

            if (flagPlayerMode.equalsIgnoreCase(PlayerMode.COMPLETED.toString())) {
                // Player mode - FETCHING
                flagPlayerMode = PlayerMode.FETCHING.toString();

                this.initialStreamSubscription();
            } else if (flagPlayerMode.equalsIgnoreCase(PlayerMode.PLAYING.toString())) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    pauseClicked = false;
                    playClicked = true;
                    // Player mode - PLAYING
                    flagPlayerMode = PlayerMode.PLAYING.toString();
                    ib_play.setImageResource(R.drawable.ic_pause);

                } else {
                    pauseAudioFile();
                }
            }

        } else {
            pauseAudioFile();
        }

    }

    /**
     * Pause audio player
     */
    private void pauseAudioFile() {

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playClicked = false;
            pauseClicked = true;
            ib_play.setImageResource(R.drawable.ic_play);

            // Player mode - PAUSED
            flagPlayerMode = PlayerMode.PAUSED.toString();
        }
    }

    @Override
    public void run() {
        int currentPosition = 0;
        int total = mediaPlayer.getDuration();
        progressBar.setMax(total);
        while (mediaPlayer != null && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = mediaPlayer.getCurrentPosition();
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }
            progressBar.setProgress(currentPosition);
        }
    }

    /**
     * initial stream request
     */
    private void initialStreamSubscription() {

        try {
            Observable<ResponseBody> observable = AcromaxPlayerService.getInstance().initialStreamRequest();

            subscription = observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onCompleted() {
                            Log.e("TAG", "On-Complete-INITIAL");
                        }

                        @Override
                        public void onError(Throwable e) {
                            System.out.print(e.getMessage());
                            onFailureStream(e);
                        }

                        @Override
                        public void onNext(ResponseBody responseStream) {
                            // set flag for initial request
                            flagStreamType = ApiRequestType.INITIAL_STREAM.toString();
                            onSuccessStream(responseStream);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * subscribe best audio stream
     *
     * @param bestAudioUri
     */
    public void bestAudioStreamSubscription(String bestAudioUri) {

        try {

            Observable<ResponseBody> observable = AcromaxPlayerService.getInstance().audioStreamRequest(bestAudioUri);

            subscription = observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onCompleted() {
                            Log.e("TAG", "On-Complete-BEST-AUDIO");
                        }

                        @Override
                        public void onError(Throwable e) {
                            System.out.print(e.getMessage());
                            onFailureStream(e);

                        }

                        @Override
                        public void onNext(ResponseBody responseStream) {
                            // set flag for best audio request
                            flagStreamType = ApiRequestType.BEST_AUDIO_STREAM.toString();
                            onSuccessStream(responseStream);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * subscribe first chunk part request
     *
     * @param audioChunk
     * @param chunkUri
     * @param offset
     * @param length
     */
    public void chunkFirstDownloadSubscription(final AudioChunk audioChunk, String chunkUri, int offset, int length) {

        try {
            String range = "bytes=" + offset + "-" + (offset + length);

            Observable<ResponseBody> observable = AcromaxPlayerService.getInstance().chunkDownloadRequest(chunkUri, range);

            subscription = observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onCompleted() {
                            Log.e("TAG", "On-Complete-Chunk-FIRST");
                        }

                        @Override
                        public void onError(Throwable e) {
                            System.out.print(e.getMessage());
                            onFailureStream(e);
                        }

                        @Override
                        public void onNext(ResponseBody responseStream) {
                            GlobalData.getInstance().setAudioChunk1(audioChunk);
                            flagStreamType = ApiRequestType.CHUNK_STREAM_FIRST.toString();
                            onSuccessStream(responseStream);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * subscribe second chunk part request
     *
     * @param audioChunk
     * @param chunkUri
     * @param offset
     * @param length
     */
    public void chunkSecondDownloadSubscription(final AudioChunk audioChunk, String chunkUri, int offset, int length) {

        try {
            // Set header param value
            String range = "bytes=" + offset + "-" + (offset + length);

            Observable<ResponseBody> observable = AcromaxPlayerService.getInstance().chunkDownloadRequest(chunkUri, range);

            subscription = observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onCompleted() {
                            Log.e("TAG", "On-Complete-Chunk-SECOND");
                        }

                        @Override
                        public void onError(Throwable e) {
                            System.out.print(e.getMessage());
                            onFailureStream(e);
                        }

                        @Override
                        public void onNext(ResponseBody responseStream) {
                            flagStreamType = ApiRequestType.CHUNK_STREAM_SECOND.toString();
                            GlobalData.getInstance().setAudioChunk2(audioChunk);
                            onSuccessStream(responseStream);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * On success response
     *
     * @param responseStream
     */
    public void onSuccessStream(ResponseBody responseStream) {

        InputStream stream = responseStream.byteStream();
        processStreamData(stream);
    }

    /**
     * On error response
     *
     * @param error
     */
    public void onFailureStream(Throwable error) {
        error.printStackTrace();
    }

    /**
     * Process response stream
     *
     * @param stream
     */
    public void processStreamData(InputStream stream) {

        String textStream = "";
        String bestAudioUri = "";
        final byte BYTE_ARRAY[] = new byte[1024];

        // check response for initial request
        if (flagStreamType.equalsIgnoreCase(ApiRequestType.INITIAL_STREAM.toString())) {

            try {
                while ((stream.read(BYTE_ARRAY)) != -1) {
                    textStream += new String(BYTE_ARRAY);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (textStream != null) {

                String[] textArray = textStream.split("\n");

                for (String streamObject : textArray) {
                    if (streamObject.contains("TYPE=AUDIO")) {
                        String[] audioArray = streamObject.split(",");

                        // get last uri from loop
                        // last uri has maximum size with quality
                        for (String audioObject : audioArray) {
                            String[] audioObjectArray = audioObject.split("=");
                            if (audioObjectArray[0].equalsIgnoreCase("URI")) {
                                bestAudioUri = audioObjectArray[1];
                                bestAudioUri = bestAudioUri.replace("\"", "");
                            }
                        }
                    }
                }
            }

            if (bestAudioUri.length() > 0) {

                // call for best audio service
                bestAudioStreamSubscription(bestAudioUri);
            }
        } else if (flagStreamType.equalsIgnoreCase(ApiRequestType.BEST_AUDIO_STREAM.toString())) {

            try {
                while ((stream.read(BYTE_ARRAY)) != -1) {
                    textStream += new String(BYTE_ARRAY);
                }
            } catch (IOException e) {

            }

            if (textStream != null) {

                String[] textArray = textStream.split("\n");

                // best audio chunk list
                audioChunkList = getChunksFromAudioStream(textArray);

                nextChunkArrive = 0;
                for (int fileIndex = 0; fileIndex < audioChunkList.size(); fileIndex = fileIndex + 2) {
                    audioChunkList.get(fileIndex).setFileName(fileIndex + ".mp3");
                    AudioChunk audioChunkNext = null;
                    if (audioChunkList.size() > fileIndex + 1) {
                        audioChunkList.get(fileIndex + 1).setFileName((fileIndex + 1) + ".mp3");
                        audioChunkNext = audioChunkList.get(fileIndex + 1);

                        try {
                            downloadAudio(audioChunkList.get(fileIndex), audioChunkNext);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        } else if (flagStreamType.equalsIgnoreCase(ApiRequestType.CHUNK_STREAM_FIRST.toString())) {

            int count = 0;
            try {
                String filename = GlobalData.getInstance().getAudioChunk1().getFileName();
                final File downloadingMediaFile = new File(getApplicationContext().getCacheDir(), filename);
                if (downloadingMediaFile.exists()) {
                    downloadingMediaFile.delete();
                }

                final OutputStream outputStream = new FileOutputStream(downloadingMediaFile);

                final byte data[] = new byte[1024];

                while ((count = stream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                }
                outputStream.flush();
                outputStream.close();
                stream.close();

                GlobalData.getInstance().getAudioChunk1().setFileStream(downloadingMediaFile);
                GlobalData.getInstance().getAudioChunk1().setFullPath(downloadingMediaFile.getAbsolutePath());
                prepareFileToPlay();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (flagStreamType.equalsIgnoreCase(ApiRequestType.CHUNK_STREAM_SECOND.toString())) {

            int count = 0;

            try {
                String filename = GlobalData.getInstance().getAudioChunk2().getFileName();
                final File downloadingMediaFile = new File(getApplicationContext().getCacheDir(), filename);
                if (downloadingMediaFile.exists()) {
                    downloadingMediaFile.delete();
                }

                final OutputStream outputStream = new FileOutputStream(downloadingMediaFile);

                final byte data[] = new byte[1024];

                while ((count = stream.read(data)) != -1) {
                    outputStream.write(data, 0, count);
                }
                outputStream.flush();
                outputStream.close();
                stream.close();

                GlobalData.getInstance().getAudioChunk2().setFileStream(downloadingMediaFile);
                GlobalData.getInstance().getAudioChunk2().setFullPath(downloadingMediaFile.getAbsolutePath());
                prepareFileToPlay();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Read all chunks from audio
     *
     * @param textArray
     * @return
     */
    public static ArrayList<AudioChunk> getChunksFromAudioStream(String[] textArray) {

        ArrayList<AudioChunk> audioChunkList = new ArrayList<>();
        int lineIndex = -1;
        int lineNameIndex = -1;

        for (String streamObject : textArray) {
            lineIndex++;
            if (streamObject.contains("EXT-X-BYTERANGE")) {

                String filterString = streamObject;

                AudioChunk audioChunk = new AudioChunk();

                String[] timeArray = filterString.split(":");

                for (String timeObject : timeArray) {
                    if (timeObject.contains("@")) {
                        String[] timeObjectArray = timeObject.split("@");
                        audioChunk.setChunkOffSet(Integer.parseInt(timeObjectArray[1]));
                        audioChunk.setChunkLength(Integer.parseInt(timeObjectArray[0]));
                    }
                }

                lineNameIndex = lineIndex + 1;
                audioChunk.setChunkFileName(textArray[lineNameIndex]);

                audioChunkList.add(audioChunk);
            }
        }
        return audioChunkList;
    }

    /**
     * Download audio parts
     *
     * @param audioChunk1
     * @param audioChunk2
     */
    private void downloadAudio(final AudioChunk audioChunk1, final AudioChunk audioChunk2) {

        GlobalData.getInstance().setAudioChunk1(audioChunk1);
        GlobalData.getInstance().setAudioChunk2(audioChunk2);

        chunkFirstDownloadSubscription(audioChunk1, audioChunk1.getChunkFileName(), audioChunk1.getChunkOffSet(), audioChunk1.getChunkLength());

        chunkSecondDownloadSubscription(audioChunk2, audioChunk2.getChunkFileName(), audioChunk2.getChunkOffSet(), audioChunk2.getChunkLength());

    }

    /**
     * Prepare file to play
     */
    private void prepareFileToPlay() {
        if (audioChunkList == null) {
            return;
        }
        nextChunkArrive++;
        if (nextChunkArrive == audioChunkList.size() - 1) {
            try {
                String filename = "audio_song.mp3";
                audioFile = new File(getApplicationContext().getCacheDir(), filename);
                if (audioFile.exists()) {
                    audioFile.delete();
                }

                OutputStream output = new FileOutputStream(audioFile);
                for (AudioChunk audioChunk : audioChunkList) {

                    if (audioChunk != null && audioChunk.getFullPath() != null) {
                        File file = new File(audioChunk.getFullPath());
                        int size = (int) file.length() - 1;
                        byte[] bytes = new byte[size];

                        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                        buf.read(bytes, 0, bytes.length);
                        buf.close();

                        output.write(bytes, 0, size);

                        file.delete();
                    }
                }
                output.flush();
                output.close();

                playAudioFile(audioFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Play audio file
     *
     * @param audioFilePlay
     */
    private void playAudioFile(File audioFilePlay) {
        try {

            FileInputStream fileInputStream = new FileInputStream(audioFilePlay);

            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(fileInputStream.getFD());
            fileInputStream.close();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                public void onCompletion(MediaPlayer mp) {
                    // Player mode - COMPLETED
                    flagPlayerMode = PlayerMode.COMPLETED.toString();
                    ib_play.setImageResource(R.drawable.ic_play);
                    mediaPlayer.reset();
                    progressBar.setProgress(0);
                    if (audioFile != null) {
                        audioFile.delete();
                    }
                }

            });
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // Player mode - PLAYING
                    flagPlayerMode = PlayerMode.PLAYING.toString();
                    ib_play.setImageResource(R.drawable.ic_pause);
                    mp.start();

                    progressBar.setProgress(0);
                    progressBar.setMax(mediaDuration);
                    new Thread(PlayerActivity.this).start();
                }
            });

            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ee) {
            ee.printStackTrace();
        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int Y = (int) event.getRawY();
        final int X = (int) event.getRawX();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                _xDelta = X - lParams.leftMargin;
                _yDelta = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view
                        .getLayoutParams();
                layoutParams.leftMargin = X - _xDelta;
                layoutParams.topMargin = Y - _yDelta;
                layoutParams.rightMargin = -250;
                layoutParams.bottomMargin = -250;
                view.setLayoutParams(layoutParams);
                break;
        }
        findViewById(R.id.rl_root).invalidate();
        return true;
    }
}
