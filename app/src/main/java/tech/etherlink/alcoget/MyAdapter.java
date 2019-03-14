package tech.etherlink.alcoget;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<docitemclass> dclist = new ArrayList<>();
    CustomItemOnClickListener listener;
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView doc_numpos;
        public TextView doc_type;
        public TextView doc_num;
        public TextView doc_date;
        public TextView doc_client;
        public TextView doc_status;
        public MyViewHolder(View v) {
            super(v);
            doc_numpos=v.findViewById(R.id.doc_numpos);
            doc_type=v.findViewById(R.id.doc_type);
            doc_num=v.findViewById(R.id.doc_num);
            doc_date=v.findViewById(R.id.doc_date);
            doc_client=v.findViewById(R.id.doc_client);
            doc_status=v.findViewById(R.id.doc_status);
        }
        public void bind(docitemclass docitemclass, int position) {
            doc_num.setText(docitemclass.name);
            if(docitemclass.type==1)
            {
                doc_type.setText("Приход");
                doc_type.setTextColor(Color.RED);
            }
            else if(docitemclass.type==2) {
                doc_type.setText("Расход");
                doc_type.setTextColor(Color.rgb(36,147,13));
            }
            else if(docitemclass.type==3)
                doc_type.setText("Списание");
            else if(docitemclass.type==4)
                doc_type.setText("Акт разногласий");
            else
                doc_type.setText("Неизв.");

            if(docitemclass.status==1)
            {
                doc_status.setText("Свободен");
                doc_status.setTextColor(Color.rgb(36,147,13));
            }
            else
            if(docitemclass.status==2)
            {
                doc_status.setText("Обработан");
                doc_status.setTextColor(Color.LTGRAY);
            }
            else
                doc_status.setText("Неизв.");

            doc_date.setText(docitemclass.date);
            doc_client.setText(docitemclass.client);
            doc_numpos.setText(String.valueOf(position+1));
        }
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bind(dclist.get(position),position);
    }

    @Override
    public int getItemCount() {
        return dclist.size();
    }

    public void setOnClickListener(CustomItemOnClickListener _listener)
    {
        this.listener = _listener;
    }


    public void setItems(Collection<docitemclass> dc) {
        dclist.addAll(dc);
        notifyDataSetChanged();
    }

    public void clearItems() {
        dclist.clear();
        notifyDataSetChanged();
    }

    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.docsitem_template, parent, false);
        final MyViewHolder vh = new MyViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view, vh.getAdapterPosition());
            }
        });

        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onLongItemClick(view, vh.getAdapterPosition());
                return true;
            }
        });
        return vh;
    }

}
