package com.example.photoeditor.graphics.commands;

import com.example.photoeditor.graphics.ConvolutionMatrix;

import android.graphics.Bitmap;

public class GaussianBlurCommand implements ImageProcessingCommand {

	private static final String ID = "com.example.photoeditor.graphics.commands.GaussianBlurCommand";
	
	private double[][] GaussianBlurConfig = new double[][] { 
			{ 1, 2, 1 }, 
			{ 2, 4, 2 },
			{ 1, 2, 1 } };

	public GaussianBlurCommand() {
	}
	
	public Bitmap process(Bitmap bitmap) {
		ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
		convMatrix.applyConfig(GaussianBlurConfig);
		convMatrix.Factor = 16;
		convMatrix.Offset = 0;
		return ConvolutionMatrix.computeConvolution(bitmap, convMatrix);
	}

	public final String getId() {
		return ID;
	}
}
