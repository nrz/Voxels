/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels;

/**
 *
 * @author otso
 */
public class Block {

    private boolean active;
    private boolean marked;

    public Block() {
        active = true;
        marked = false;
    }


    public boolean isActive() {
        return active;
    }
    public boolean isMarked() {
        return marked;
    }

    public void deactivate() {
        this.active = false;
    }
    public void markToDeactivate() {
        this.marked = true;
    }

    public void activate() {
        this.active = true;
    }

}
