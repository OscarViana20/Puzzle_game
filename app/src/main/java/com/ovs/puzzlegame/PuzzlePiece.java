package com.ovs.puzzlegame;


import android.content.Context;

import androidx.appcompat.widget.AppCompatImageView;

public class PuzzlePiece extends AppCompatImageView {

    public int xCord;
    public int yCord;
    public int pieceWidth;
    public int pieceHeight;
    public boolean canMove = true;

    public PuzzlePiece(Context context) {
        super(context);
    }
}