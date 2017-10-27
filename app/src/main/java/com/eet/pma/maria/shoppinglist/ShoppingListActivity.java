package com.eet.pma.maria.shoppinglist;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ShoppingListActivity extends AppCompatActivity {

    private static final String FILENAME = "shopping_list.txt"; //static = solo hay una copia de esta clase para todos los datos
    private static final int MAX_BYTES = 8000;                 //final  = porque no cambiara

    private ArrayList<ShoppingItem> item_list; //llista amb tots els elements; després l'utilitzarem juntament amb l'adapter
    private ShoppingListAdapter adapter;

    private ListView list;
    private Button btn_add;
    private EditText edit_item;

    private void writeItemList(){
        /*
        Patates;false
        Sabó;false
        Formatge;false
        */

        try {
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            for(int i = 0; i < item_list.size(); i++){
                ShoppingItem it = item_list.get(i);
                String line = String.format("%s;%b\n", it.getText(), it.isChecked()); //guarda el que tenim per linia

                fos.write(line.getBytes()); //exception IOException e
            }
            fos.close();
        } catch (FileNotFoundException e) {
            //para 'cazar' excepciones, saber qual es el error
            Log.e("Maria","writeItemList: FileNotFoundException");
            Toast.makeText(this, R.string.cannot_write, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("Maria","writeItemList: FileNotFoundException");
            Toast.makeText(this, R.string.cannot_write, Toast.LENGTH_LONG).show();
        }
    }

    protected void readItemList(){
        item_list = new ArrayList<>();

        try {
            FileInputStream fis = openFileInput(FILENAME);
            byte[] buffer = new byte[MAX_BYTES];
            int nread = fis.read(buffer); //nº bytes llegits

            if(nread > 0) {
                String content = new String(buffer, 0, nread); //1ra posicion = 0; nread = longuitud
                String[] lines = content.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    String[] parts = lines[i].split(";");
                    item_list.add(new ShoppingItem(parts[0], parts[1].equals("true")));
                }
            }
            
            fis.close();
        } catch (FileNotFoundException e) {
            //voy a abrir el fichero y me dice que no lo encuentra (cuando abrimos la app por 1ra vez)
            Log.i("Maria", "readItemList: FileNotFoundException");
        } catch (IOException e) {
            Log.e("Maria","readItemList: FileNotFoundException");
            Toast.makeText(this, R.string.cannot_read, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        writeItemList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        list = (ListView) findViewById(R.id.llista);
        btn_add = (Button) findViewById(R.id.btn_afegir);
        edit_item = (EditText) findViewById(R.id.edit_item);

        readItemList(); //crea llista buida per primer cop

        adapter = new ShoppingListAdapter(this, R.layout.shopping_item,item_list);

        edit_item.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                addItem(); //afegir element amb el teclat('done')
                return true;
            }
        });

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                //per saber quan clickem un element
                item_list.get(pos).toggleChecked(); //canvia el valor de checked a no checked, i a l'inversa
                adapter.notifyDataSetChanged();
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() { //accio de borrar element
            @Override
            public boolean onItemLongClick(AdapterView<?> list, View item, int pos, long id) {
                maybeRemoveItem(pos);
                return true;
            }
        });
    }


    //afegir element amb el botó de '+'
    public void afegir(View view) {
        addItem();
    }

    private void addItem() {
        String item_text = edit_item.getText().toString();

        if(!item_text.isEmpty()) {     //alternativa = !item_text.equals("")
            item_list.add(new ShoppingItem(item_text));
            adapter.notifyDataSetChanged();
            edit_item.setText("");
        }
        list.smoothScrollToPosition(item_list.size()-1); //perquè la llista es mogui sola fins a l'element que estem afegint
    }

    //preguntem si volem borrar element, si és així ho elimina
    private void maybeRemoveItem(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm);
        String fmt = getResources().getString(R.string.missatge);
        builder.setMessage(String.format(fmt,item_list.get(pos).getText()));

        builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                item_list.remove(pos);
                adapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton(android.R.string.cancel,null);
        builder.create().show();
    }

    @Override //rellenar el menu
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override //tenemos la opcion de menu seleccionada
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.borrar_marcados:
                clearChecked();
                return true;
            case R.id.borrar_todo:
                clearAll();
                return true;
            default:
                return super.onOptionsItemSelected(item); //si ocurre un error llama el metodo de la clase 'padre'
        }
    }

    //borrar los marcados
    private void clearChecked() {
        int i = 0;
        while(i < item_list.size()){
            ShoppingItem it = item_list.get(i);
            boolean b = it.isChecked();
            if(b){
                item_list.remove(i);
            }
            else{
                i++; //si hay dos elementos checked seguidos, el segundo no lo miraría
            }
        }
        adapter.notifyDataSetChanged();
    }

    //borrar todos
    private void clearAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm);
        builder.setMessage(R.string.confirm_all);
        builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                item_list.clear();
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(android.R.string.cancel,null);
        builder.create().show();
    }
}
