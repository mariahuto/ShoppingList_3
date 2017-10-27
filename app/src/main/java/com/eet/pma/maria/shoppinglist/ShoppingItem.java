package com.eet.pma.maria.shoppinglist;

/**
 * Created by Maria on 21/10/2017.
 */

public class ShoppingItem {
    private String text;
    private boolean checked; //guarda si l'element de la llista està marcat

    public ShoppingItem(String text) { //inicializa el texto
        this.text = text;
        this.checked = false; //no faria falta, perquè s'inicialitza a false encara que no ho determinem
    }

    public ShoppingItem(String text, boolean checked) { //inicializa el texto con el check
        this.text = text;
        this.checked = checked;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void toggleChecked() {
        this.checked = !this.checked;
        /*ShoppingItem item = item_list.get(pos);
         boolean checked = item.isChecked();
         item_list.get(pos).setChecked(!checked); //guardar que hem clickat aquell element*/
    }
}
