package jp.beacrew.locotester;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

public class ImageArrayAdapter extends ArrayAdapter{
    private int resourceId;
    private List<ListItem> items;
    private LayoutInflater inflater;
    private ImageArrayAdapterCallback imageArrayAdapterCallback;

    interface ImageArrayAdapterCallback {
        void onSwitchCheck(int position);
    }


    public ImageArrayAdapter(Context context, int resourceId, List<ListItem> items, ImageArrayAdapterCallback imageArrayAdapterCallback) {
        super(context, resourceId, items);

        this.resourceId = resourceId;
        this.items = items;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.imageArrayAdapterCallback = imageArrayAdapterCallback;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;

        ListItem item = this.items.get(position);

        if (item.getCluseterName() == null) {
            view = this.inflater.inflate(this.resourceId, null);
        } else {
            view = this.inflater.inflate(R.layout.list_view_section_item, null);
        }

        if(item.getCluseterName() == null) {

            Switch advertiseSwitch = (Switch) view.findViewById(R.id.switch1);
            if (advertiseSwitch != null) {
                advertiseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Log.d("advertiseTest", "OnSwitchClick:" + position);
                        imageArrayAdapterCallback.onSwitchCheck(position);
                    }
                });

                // テキストをセット
                TextView appInfoText = (TextView) view.findViewById(R.id.item_text);
                appInfoText.setText(item.getText());

                TextView appInfoSubText = (TextView) view.findViewById(R.id.item_subtext);
                appInfoSubText.setText(item.getSubText());

                // アイコンをセット
                ImageView appInfoImage = (ImageView) view.findViewById(R.id.item_image);
                appInfoImage.setImageResource(item.getImageId());
                appInfoImage.setAdjustViewBounds(true);
            }
        } else {
            // テキストをセット
            TextView appInfoText = (TextView) view.findViewById(R.id.section_text);
            appInfoText.setText(item.getCluseterName());

        }

        return view;
    }


}
