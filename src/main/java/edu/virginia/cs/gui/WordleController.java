package edu.virginia.cs.gui;

import edu.virginia.cs.wordle.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

import static java.lang.Character.*;

public class WordleController {
    public Label wordleLabel;
    @FXML
    private Label gameResult;
    @FXML
    private Label remainingGuesses;
    @FXML
    private Label playAgain;
    @FXML
    private GridPane guessGrid;
    @FXML
    private Label invalidWord;
    @FXML
    private Stage stage;
    @FXML
    private Label answer;
    @FXML
    private Button noButton;
    @FXML
    private Button yesButton;


    Wordle wordle;


    @FXML
    public void initialize(){
        wordle = new WordleImplementation();
        clearBoard();
        clearFields();
        ObservableList<Node> children = guessGrid.getChildren();
        disableAllButFirstField(children);
    }

    private static void disableAllButFirstField(ObservableList<Node> children) {
        for(Node node: children){
            node.setMouseTransparent(true);
            //HERE
            if(!(children.indexOf(node) == 0)){
                TextField letter = (TextField) node;
                letter.setEditable(false);
            }
            else{
                TextField letter = (TextField) node;
                letter.setEditable(true);
            }
        }
    }

    private void clearFields() {
        yesButton.setVisible(false);
        noButton.setVisible(false);
        answer.setText("");
        playAgain.setText("");
        gameResult.setText("");
        remainingGuesses.setText("");
    }

    private void clearBoard(){
        ObservableList<Node> children = guessGrid.getChildren();
        for(Node n: children) {
            TextField cur = (TextField)n;
            cur.setText("");
            cur.setStyle("-fx-background-color: #FFFFFF; -fx-text-inner-color: #000000;");
        }
    }
    @FXML
    public void restart() {
        initialize();
    }

    @FXML
    protected void handleEntryFirstFour(KeyEvent e) {
        TextField curr = (TextField) e.getSource();
        if(!wordle.isWin()){


            //  https://stackoverflow.com/questions/20825935/javafx-get-node-by-row-and-column
            TextField next = getNextTextField(curr);
            moveCursorForwardIfValidLetter(curr, next);}
        else{
            curr.setEditable(false);
            curr.setMouseTransparent(false);
        }
    }

    @FXML
    public void handleBackspace(KeyEvent e) {
        if(!wordle.isWin()) {
            TextField curr = (TextField) e.getSource();
            TextField prev = getNthPrevTextField(curr, 1);
            if (e.getCode().equals(KeyCode.BACK_SPACE)) {
                moveCursorBackwards(curr, prev);
            }
        }
    }
    private TextField getNthPrevTextField(TextField curr, int n) {
        TextField prev;
        ObservableList<Node> children = guessGrid.getChildren();
        int nextInd = children.indexOf(curr) - n;
        prev = (TextField) children.get(nextInd);
        return prev;
    }

    private void moveCursorBackwards(TextField curr, TextField prev) {
        if(!wordle.isWin()) {
            if (curr.getText().equals("")) {
                prev.requestFocus();
                prev.setText("");
                prev.setEditable(true);
            } else {
                curr.setText("");
                curr.setEditable(true);
            }
        }

    }

    private TextField getNextTextField(TextField curr) {
        ObservableList<Node> children = guessGrid.getChildren();
        int nextInd = children.indexOf(curr) + 1;
        TextField next = (TextField) children.get(nextInd);
        return next;
    }


    private void moveCursorForwardIfValidLetter(TextField curr, TextField next) {
        if(!wordle.isWin()) {
            try {
                char entry = curr.getText().charAt(0);
                if (isLetter(entry)) {
                    //https://stackoverflow.com/questions/33478752/how-to-move-the-text-pointer-from-one-field-to-another-in-java
                    if (isLowerCase(entry)) {
                        curr.setText(Character.toString(toUpperCase(entry)));
                    }
                    curr.setEditable(false);
                    if (curr.isFocused()) {

                        next.requestFocus();
                        next.setEditable(true);
                    }
                } else {
                    curr.setText("");
                }
            } catch (Exception e) {
                throw new RuntimeException("Waiting for a character");
            }
        }
    }


    public void handleLastCharacter(KeyEvent e) {
        TextField curr = (TextField) e.getSource();
        char entry = curr.getText().charAt(0);
        if(isLetter(entry)){
            if(isLowerCase(entry)){
                curr.setText(Character.toString(toUpperCase(entry)));
            }
            curr.setEditable(false);
        }
        else{
            curr.setText("");
        }

        String guess = generateGuessFromFields(curr);

        try {
            invalidWord.setText("");
            LetterResult[] results = wordle.submitGuess(guess);
            if(results != null){
                setLetterColors(results, curr);
                handleGuessResult(curr);
            }
        } catch (RuntimeException e1) {
            setRestartGuess(curr);
        }
    }

    private void handleGuessResult(TextField curr) {
        if (!wordle.isWin()) {
            handleLoss();
            int ind = guessGrid.getChildren().indexOf(curr);
            TextField startOfNextLine = (TextField) guessGrid.getChildren().get(ind+1);
            startOfNextLine.requestFocus();
            startOfNextLine.setEditable(true);
        } else {
            gameResult.setText("Winner!");
            remainingGuesses.setText("You won in " + (6-wordle.getRemainingGuesses()) + " guesses");
            preventFurtherEdits();
        }
    }

    private void handleLoss() {
        if (wordle.isLoss()) {
            invalidWord.setText("");
            gameResult.setText("Game Over, You Lose!");
            answer.setText("The answer was " + wordle.getAnswer());
            preventFurtherEdits();
        }
    }

    private void preventFurtherEdits() {
        for (Node node : guessGrid.getChildren()) {
            TextField n = (TextField) node;
            n.setEditable(false);
            n.setMouseTransparent(true);
        }
        playAgain.setText("Would you like to play again?");
        yesButton.setVisible(true);
        noButton.setVisible(true);
    }

    private void setRestartGuess(TextField last) {
        if(wordle.isWin()) return;
        if(!wordle.isLoss()) invalidWord.setText("Invalid word");
        if(wordle.isLoss()) return;

        invalidWord.setStyle("-fx-text-fill: red");
        TextField[] guessNodeArray = getNodeArray(last);
        guessNodeArray[0].requestFocus();
        for (int i = 0; i <= 4; i++) {
            TextField curr = guessNodeArray[i];
            curr.setText("");
            if(i==0) curr.setEditable(true);
        }
    }

    private void setLetterColors(LetterResult[] results, TextField last) {

        TextField[] guessNodeArray = getNodeArray(last);
        for(int i=0; i<=4; i++){
            TextField cur = guessNodeArray[i];
            cur.setStyle("-fx-text-inner-color: #FFFFFF;");
            cur.setEditable(false);
            cur.setMouseTransparent(true);
            if(results[i].equals(LetterResult.GRAY)){
                //cur.setStyle("-fx-background-color: #999999;");
                cur.setStyle("-fx-background-color: #787c7f; -fx-text-inner-color: #FFFFFF;");

            }
            if(results[i].equals(LetterResult.GREEN)){
                //cur.setStyle("-fx-background-color: #00FF00;");
                cur.setStyle("-fx-background-color: #6ca965; -fx-text-inner-color: #FFFFFF;");
            }
            if(results[i].equals(LetterResult.YELLOW)){
                //cur.setStyle("-fx-background-color: #ffff00;");
                cur.setStyle("-fx-background-color: #c8b653; -fx-text-inner-color: #FFFFFF;");

            }
        }
    }
    private TextField[] getNodeArray(TextField last){
        TextField[] guess = new TextField[5];
        int counter = 4;
        for(int i=0; i<=4; i++){
            guess[i] = getNthPrevTextField(last, counter);
            counter--;
        }
//        guess[4] = getNthPrevTextField(last, 0);
//        guess[3] = getNthPrevTextField(last, 1);
//        guess[2] = getNthPrevTextField(last, 2);
//        guess[1] = getNthPrevTextField(last, 3);
//        guess[0] = getNthPrevTextField(last, 4);

        return guess;
    }


    private String generateGuessFromFields(TextField last) {
        String guess = "";
        for(int i=4; i>=0; i--) {
            TextField prev = getNthPrevTextField(last, i);
            guess += prev.getText();
        }
        return guess;
    }


    public void close(ActionEvent actionEvent) {
        Platform.exit();
    }
}