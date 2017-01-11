package fr.onagui.config;

import java.awt.Color;

public interface ScoreColorConfiguration {

	public Color getColorFromScore(double score);

	public Color getColorImpossibleToAlign();

	public Color getColorNeutral();

}