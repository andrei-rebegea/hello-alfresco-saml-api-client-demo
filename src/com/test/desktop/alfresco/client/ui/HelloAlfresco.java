package com.test.desktop.alfresco.client.ui;

import com.test.desktop.alfresco.client.controller.HelloAlfrescoController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class HelloAlfresco extends Application
{

    Stage primaryStage;
    Scene internalBrowserSection;
    BrowserAlfresco internalBrowserWrapper;
    TextArea textAreaForLog;

    HelloAlfrescoController alfrescoController = new HelloAlfrescoController();

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage stage)
    {
        primaryStage = stage;
        alfrescoController.init();

        primaryStage.setTitle("Hello Alfresco");

        alfrescoController.checkAlfrescoSAMLSettings();
        alfrescoController.printCookies();

        initComponents();

        primaryStage.setScene(getButtonFirstStage());
        primaryStage.show();
    }

    void initComponents()
    {
        internalBrowserWrapper = createBrowser();
        internalBrowserSection = getBrowserScene(internalBrowserWrapper);
    }

    Scene getButtonFirstStage()
    {
        Pane buttonStage = new TilePane();

        Label infoLabel = new Label("Response from: " + HelloAlfrescoController.ALFRESCO_ENABLED);
        buttonStage.getChildren().add(infoLabel);
        if (alfrescoController.getInfo() == null)
        {
            createSAMLSettingsNotFoundSection(buttonStage);
        }
        else
        {
            createSAMLSettingsFoundSection(buttonStage);
        }
        Scene buttonScene = new Scene(buttonStage, 900, 250);
        return buttonScene;
    }

    Scene getBrowserScene(BrowserAlfresco browser)
    {
        return new Scene(browser, 900, 600, Color.web("#666970"));
    }

    private BrowserAlfresco createBrowser()
    {
        return new BrowserAlfresco();
    }

    void createSAMLSettingsNotFoundSection(Pane buttonStage)
    {
        Label infoLabel1 = new Label("Error or no response");
        buttonStage.getChildren().add(infoLabel1);
    }

    void createSAMLSettingsFoundSection(Pane buttonStage)
    {
        Label infoLabel1 = new Label("Is SAML enabled:  " + alfrescoController.getInfo().isSamlEnabled);
        Label infoLabel2 = new Label("Is SAML enforced: " + alfrescoController.getInfo().isSamlEnforced);
        Label infoLabel3 = new Label("IdP description:  " + alfrescoController.getInfo().idpDescription);
        Label infoLabel4 = new Label("Tenant:           " + alfrescoController.getInfo().tenantDomain);
        buttonStage.getChildren().add(infoLabel1);
        buttonStage.getChildren().add(infoLabel2);
        buttonStage.getChildren().add(infoLabel3);
        buttonStage.getChildren().add(infoLabel4);

        Button btn = new Button();
        btn.setText("Let's dance!");
        btn.setOnAction(new EventHandler<ActionEvent>()
        {

            @Override
            public void handle(ActionEvent event)
            {
                dance();
            }
        });

        buttonStage.getChildren().add(btn);
    }

    protected void dance()
    {
        primaryStage.setScene(internalBrowserSection);
        internalBrowserWrapper.webEngine.load(HelloAlfrescoController.ALFRESCO_AUTHENTICATE);

        // browser.webEngine.
        // we can do it better
        Runnable monitor = new Runnable()
        {

            @Override
            public void run()
            {
                waitForReturn();
            }
        };
        new Thread(monitor).start();
    }

    protected void waitForReturn()
    {
        while (!internalBrowserWrapper.webEngine.getLocation().startsWith(HelloAlfrescoController.ALFRESCO_AUTHENTICATE_RESPONSE)
                && !internalBrowserWrapper.webEngine.getLocation().startsWith(HelloAlfrescoController.ALFRESCO_AUTHENTICATE_RESPONSE_S))
        {
            try
            {
                Thread.sleep(300);
            }
            catch (Exception e)
            {
                // so what
            }
        }
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                // Update UI here
                displayResultPage();
            }
        });
    }

    void displayResultPage()
    {
        String location = internalBrowserWrapper.webEngine.getLocation();
        System.out.println(location);
       
   
        final javafx.scene.web.WebEngine webEngine = internalBrowserWrapper.webEngine;
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                org.w3c.dom.Document doc = webEngine.getDocument();
                loaded();
            }
        });
       // webEngine.loadContent("<h1>hello</h1>");
        
       // displayFunctionButtons();
    }

    public void displayFunctionButtons()
    {
        {
            if (internalBrowserWrapper.webEngine.getDocument() == null)
            {
                System.out.println("1 getDocument is null");
            }
            if (internalBrowserWrapper.webEngine.getDocument().getDocumentElement() == null)
            {
                System.out.println("2 getDocumentElement is null");
            }
        }
        String pageText = internalBrowserWrapper.webEngine.getDocument().getDocumentElement().getTextContent();

        // =========================== Controller part ==========================
        alfrescoController.printCookies();
        alfrescoController.parseTicket(pageText);
        // ======================================================================

        textAreaForLog = new TextArea();
        textAreaForLog.appendText("Response from: Authenticaton process\n\n");
        textAreaForLog.appendText("ticket and user: " + pageText + "\n");
        textAreaForLog.appendText("userID: " + alfrescoController.getUserID() + " ticket: " + alfrescoController.getTicket() + "\n");
        if (alfrescoController.getImpCookie() != null)
        {
            textAreaForLog.appendText("jsession info: " + alfrescoController.getImpCookie() + "\n");
        }
        else
        {
            textAreaForLog.appendText(alfrescoController.getGenericSessionErrorMessage());
        }
        textAreaForLog.appendText("==================================================\n\n");

        Pane buttonsPane = new FlowPane();
        {
            // remove all cookies
            Button btn = new Button();
            btn.setText("Delete Cookies");
            btn.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent event)
                {
                    textAreaForLog.appendText(alfrescoController.removeAllCookies());
                }
            });
            buttonsPane.getChildren().add(btn);
        }

        {
            // add the good cookie
            Button btn = new Button();
            btn.setText("Add Good Alfresco Cookie");
            btn.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent event)
                {
                    textAreaForLog.appendText(alfrescoController.addGoodCookie());
                }
            });
            buttonsPane.getChildren().add(btn);
        }
        {
            // list all sites button
            Button btn = new Button();
            btn.setText("List all Sites in Alfresco");
            btn.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent event)
                {
                    textAreaForLog.appendText(alfrescoController.listAllSites());
                }
            });
            buttonsPane.getChildren().add(btn);
        }

        {
            // get a file
            Button btn = new Button();
            btn.setText("Get a File");
            btn.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent event)
                {
                    textAreaForLog.appendText(alfrescoController.getAFile());
                }
            });
            buttonsPane.getChildren().add(btn);
        }

        {
            // get a file
            Button btn = new Button();
            btn.setText("Get a File with Ticket");
            btn.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent event)
                {
                    textAreaForLog.appendText(alfrescoController.getAFileWithTicket());
                }
            });
            buttonsPane.getChildren().add(btn);
        }

        {
            // Cmis get a file
            Button btn = new Button();
            btn.setText("CMIS get a file");
            btn.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent event)
                {
                    textAreaForLog.appendText(alfrescoController.getAFileWithCMIS());
                }
            });
            buttonsPane.getChildren().add(btn);
        }
        {
            // Cmis get a file with ticket
            Button btn = new Button();
            btn.setText("CMIS get a file with Ticket");
            btn.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent event)
                {
                    textAreaForLog.appendText(alfrescoController.getAFileWithCMISWithTicket());
                }
            });
            buttonsPane.getChildren().add(btn);
        }

        Pane resultsScene2 = new FlowPane();
        resultsScene2.getChildren().add(buttonsPane);
        resultsScene2.getChildren().add(textAreaForLog);
        textAreaForLog.setPrefWidth(600);
        textAreaForLog.setPrefHeight(400);

        Scene resultsScene = new Scene(resultsScene2, 610, 610);

        primaryStage.setScene(resultsScene);
    }

    public void loaded()
    {
        System.out.println("ready loaded");
        displayFunctionButtons();
    }
}
