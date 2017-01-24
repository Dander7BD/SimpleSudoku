/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import gamelogic.Sudoku4x4;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author Dan Andersson
 */
public class SimpleSudoku extends Application
{
    private final Sudoku4x4 gameInstance = new Sudoku4x4();
    
    @Override
    public void start(Stage primaryStage)
    {
        StackPane root = new StackPane();
        
        // create the textfields
        for(int row = 0; row < 4; ++row) for(int col = 0; col < 4; ++col)
        {
            TextField control = new TextField();
            
            control.setLayoutX(((double)col) * control.getWidth());
            control.setLayoutX(((double)row) * control.getHeight());
            
            int value = this.gameInstance.get(row, col);
            if( value != 0)
            {
                control.setText(Integer.toString(value));
                control.setEditable(false);
            }
            else
            {
                // todo: bind onValueChanged event handler
            }
            
            root.getChildren().add(control);
        }
        
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>()
        {
            
            @Override
            public void handle(ActionEvent event)
            {
                System.out.println("Hello World!");
            }
        });
        
        //root.getChildren().add(btn);
        
        Scene scene = new Scene(root, 300, 250);
        
        primaryStage.setTitle("Simple Sudoku");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }
    
}
