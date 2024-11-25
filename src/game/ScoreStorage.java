package game;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;

public class ScoreStorage {
//    private static final String SCORE_FILE = "src/resource/score.json";
    
	static String jarPath = new File(FlappyBird.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
	static String highScoreFilePath = jarPath + File.separator + "scores.json";

	
	private static final String SCORE_FILE = highScoreFilePath;

    // Class to hold the score data
    public static class ScoreData {
        public double highScore;
        
        public ScoreData(double highScore) {
            this.highScore = highScore;
        }
    }

    // Save the high score to a JSON file
    public static void saveHighScore(double highScore) {
        Gson gson = new Gson();
        ScoreData scoreData = new ScoreData(highScore);

        try (FileWriter writer = new FileWriter(SCORE_FILE)) {
            gson.toJson(scoreData, writer);
            System.out.println("High score saved successfully!");
        } catch (IOException e) {
            System.out.println("Failed to save high score: " + e.getMessage());
        }
    }

    // Load the high score from a JSON file
    public static double loadHighScore() {
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(SCORE_FILE)) {
            ScoreData scoreData = gson.fromJson(reader, ScoreData.class);
            return scoreData.highScore;
        } catch (IOException e) {
            System.out.println("No saved high score found. Starting fresh.");
            return 0; 
        }
    }
}
