/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author jlewis
 */
public class MenuControlListener extends MouseAdapter 
{
    
   @Override
   public void mousePressed(MouseEvent e)
   {
       System.out.println("we see yo clicked");
       
   }
    
    @Override
    public void mouseClicked(MouseEvent e) 
    {
        System.out.println("here");
//        this.mousePressed(e);
    }
    @Override
    public void mouseEntered(MouseEvent e)
    {
//        e.getComponent().setFocusable(true);
//        e.getComponent().requestFocus();
        System.out.println("ENTER");
    }
    @Override
    public void mouseExited(MouseEvent e)
    {
//        e.getComponent().setFocusable(false);
        System.out.println("EXIT");
    }
}
