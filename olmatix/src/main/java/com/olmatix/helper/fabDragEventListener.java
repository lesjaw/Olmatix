package com.olmatix.helper;

import android.graphics.Color;
import android.view.DragEvent;
import android.view.View;

/**
 * Created by Lesjaw on 14/01/2017.
 */

public class fabDragEventListener {
    //This is the method that the system calls when it dispatches a drag event to the
    // listener.
    public boolean onDrag(View v, DragEvent event) {

        // Defines a variable to store the action type for the incoming event
        final int action = event.getAction();
        switch (action) {
            case DragEvent.ACTION_DRAG_ENTERED:
                v.setBackgroundColor(Color.GRAY);
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                //v.setBackgroundColor(Color.TRANSPARENT);
                return true;

            case DragEvent.ACTION_DRAG_STARTED:
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
                //v.setVisibility(View.VISIBLE);
                return false;
            // return processDragStarted(event);
            case DragEvent.ACTION_DROP:
                //Toast.makeText(context, "teste ok", Toast.LENGTH_SHORT).show();;
            default:
                return true;

        }
    }
}