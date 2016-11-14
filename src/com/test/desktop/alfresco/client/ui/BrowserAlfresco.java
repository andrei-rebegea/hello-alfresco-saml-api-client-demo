package com.test.desktop.alfresco.client.ui;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class BrowserAlfresco extends Region
{

    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();

    public BrowserAlfresco()
    {
        // apply the styles
        getStyleClass().add("browser");
        // load the web page
        getChildren().add(browser);

    }


    @Override
    protected void layoutChildren()
    {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computePrefWidth(double height)
    {
        return 900;
    }

    @Override
    protected double computePrefHeight(double width)
    {
        return 600;
    }
}