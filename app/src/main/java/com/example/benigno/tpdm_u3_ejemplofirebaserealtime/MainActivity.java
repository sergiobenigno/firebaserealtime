package com.example.benigno.tpdm_u3_ejemplofirebaserealtime;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    EditText nombre, telefono, domicilio;
    Button insertar, eliminar,mostrar;
    private DatabaseReference mDatabase;
    List<Usuario> datos;
    ListView listado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nombre = findViewById(R.id.nombre);
        domicilio = findViewById(R.id.domicilio);
        telefono = findViewById(R.id.telefono);
        insertar = findViewById(R.id.insertar);
        eliminar = findViewById(R.id.eliminar);
        mostrar = findViewById(R.id.mostrar);
        listado = findViewById(R.id.lista);


        mDatabase = FirebaseDatabase.getInstance().getReference();


        insertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Usuario n = new Usuario(nombre.getText().toString(), domicilio.getText().toString(), telefono.getText().toString());
                if(n.telefono.isEmpty() || n.domicilio.isEmpty() || n.nombre.isEmpty()){
                    Toast.makeText(MainActivity.this, "EXISTEN CAMPOS VACIOS", Toast.LENGTH_SHORT).show();
                    return;
                }
                mDatabase.child("usuario").child(n.telefono).setValue(n)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this, "EXITO", Toast.LENGTH_SHORT).show();
                                nombre.setText("");domicilio.setText("");telefono.setText("");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "ERROR!!!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pedirEliminar();

            }
        });


        mostrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pedirMostrar();
            }
        });

        //llena la lista con datos
        mDatabase.child("usuario").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                datos = new ArrayList<>();

                if(dataSnapshot.getChildrenCount()<=0){
                    Toast.makeText(MainActivity.this, "ERROR: No hay datos", Toast.LENGTH_SHORT).show();
                    return;
                }

                for(final DataSnapshot snap : dataSnapshot.getChildren()){
                    mDatabase.child("usuario").child(snap.getKey()).addValueEventListener(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Usuario u = dataSnapshot.getValue(Usuario.class);

                                    if(u!=null){
                                        datos.add(u);
                                    }
                                    cargarSelect();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            }
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void cargarSelect(){
        if (datos.size()==0) return;
        String nombres[] = new String[datos.size()];

        for(int i = 0; i<nombres.length; i++){
            Usuario u = datos.get(i);
            nombres[i] = u.nombre;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombres);
        listado.setAdapter(adapter);
    }

    private void pedirEliminar(){
        final EditText id = new EditText(this);
        id.setHint("TELEFONO A ELIMINAR");
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("ATENCION").setMessage("VALOR A BUSCAR:").setView(id).setPositiveButton("ELIMINAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eliminar(id.getText().toString());
            }
        }).setNegativeButton("Cancelar", null).show();
    }

    private void eliminar(String id){
        mDatabase.child("usuario").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "SE ELIMINO CORRECTAMENTE", Toast.LENGTH_SHORT).show();
                        telefono.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "ERROR AL ELIMINAR", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pedirMostrar(){
        final EditText id = new EditText(this);
        id.setHint("ID A BUSCAR");
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("ATENCION").setMessage("VALOR A BUSCAR:").setView(id).setPositiveButton("Buscar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mostrar(id.getText().toString());
            }
        }).setNegativeButton("Cancelar", null).show();
    }


    private void mostrar(String i){
        FirebaseDatabase.getInstance().getReference().child("usuario").child(i)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Usuario usseer = dataSnapshot.getValue(Usuario.class);

                        if(usseer!=null) {
                            nombre.setText(usseer.nombre);
                            domicilio.setText(usseer.domicilio);
                            telefono.setText(usseer.telefono);
                        } else {
                            mensaje("Error","No se encontró dato a mostrar");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void mensaje(String t, String m){
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);

        alerta.setTitle(t).setMessage(m).setPositiveButton("OK",null).show();
    }
}
