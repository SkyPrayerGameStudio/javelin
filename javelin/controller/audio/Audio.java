package javelin.controller.audio;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javelin.Javelin;
import javelin.model.unit.Monster;

/**
 * Looks up, loads, caches and plays audio.
 *
 * Given an action A, the filename lookup for a given {@link Monster} is in the
 * order: "A-name", A-type, "A".
 *
 * TODO this should probably be entirely scrapper in favor of JavaFX internal
 * media player
 *
 * @author alex
 */
public class Audio{
	/**
	 * Cannot easily support cross-platform MP3 and OGG with any up-to-date.
	 * Tritonus doesn't seem to work with Linux 64b. TODO JaveFX
	 */
	static final List<String> EXTENSIONS=List.of(".wav");
	static final Map<String,Clip> CACHE=new HashMap<>();

	class NoAudio extends IOException{
		public NoAudio(Throwable cause){
			super(cause);
		}

		public NoAudio(String message){
			super(message);
		}
	}

	String action;
	Monster monster;
	Clip clip=null;

	public Audio(String action,Monster m){
		this.action=action;
		monster=m;
	}

	File lookup() throws NoAudio{
		var byname=action+"-"+monster.name.replaceAll(" ","");
		var bytype=action+"-"+monster.type;
		for(var filename:List.of(byname,bytype,action)){
			filename=filename.toLowerCase();
			for(var extension:EXTENSIONS){
				var f=new File("audio",filename+extension);
				if(f.exists()) return f;
			}
		}
		throw new NoAudio("Cannot find audio for "+this);
	}

	/** @param action Play this sound loop. */
	void load(){
		File f=null;
		try{
			clip=CACHE.get(action+monster);
			if(clip==null){
				f=lookup();
				try(var stream=AudioSystem.getAudioInputStream(f)){
					clip=AudioSystem.getClip();
					clip.open(stream);
				}
				CACHE.put(action+monster,clip);
			}
		}catch(UnsupportedAudioFileException|LineUnavailableException
				|IOException e){
			if(Javelin.DEBUG) throw new RuntimeException(f.getPath(),e);
			e.printStackTrace();
		}
	}

	/** Loads (possibly from cache) and playes this audio. */
	public void play(){
		load();
		if(clip!=null){
			clip.setFramePosition(0);
			clip.start();
		}
	}

	@Override
	public String toString(){
		return action+" ("+monster+")";
	}
}