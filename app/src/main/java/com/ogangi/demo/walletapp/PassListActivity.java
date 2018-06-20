package com.ogangi.demo.walletapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ogangi.oneworldwallets.wallet.entities.PassWallet;
import com.ogangi.oneworldwallets.wallet.sdk.WalletManager;
import com.ogangi.oneworldwallets.wallet.views.pass.OnPassInteractionListener;
import com.ogangi.oneworldwallets.wallet.views.pass.PassContainerFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Passes. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PassDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class PassListActivity extends AppCompatActivity implements OnPassInteractionListener, UrlDialogFragment.NoticeDialogListener {
    private SimpleItemRecyclerViewAdapter adapter;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "wallet.demo.update" is broadcasted.
    private BroadcastReceiver passReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String passId = intent.getStringExtra("id");
            PassWallet pass = WalletManager.getInstance().getPass(passId);
            adapter.addPass(pass);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            //Add Pass
            DialogFragment dialog = new UrlDialogFragment();
            dialog.show(getSupportFragmentManager(), "addPass");
        });

        if (findViewById(R.id.pass_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.pass_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //Update Adapter
        List<PassWallet> myPasses = WalletManager.getInstance().getPasses();
        adapter.updatePasses(myPasses);
        //UpdateUI Manager
        // Register to receive messages with actions named "wallet.demo.update".
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(passReceiver , new IntentFilter("wallet.demo.update"));
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is in background
        LocalBroadcastManager.getInstance(this).unregisterReceiver(passReceiver);
        super.onPause();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        adapter = new SimpleItemRecyclerViewAdapter(this, mTwoPane);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onPassDeleted() {
        //When Pass is Deleted on Two Pane View
        if(mTwoPane){

        }
    }

    @Override
    public void onDialogPositiveClick(UrlDialogFragment dialog, String url) {
        WalletManager.getInstance().addPass(Uri.parse(url));
        dialog.dismiss();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final PassListActivity mParentActivity;
        private final List<PassWallet> passes;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PassWallet pass = (PassWallet) view.getTag();
                if (mTwoPane) {
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.pass_detail_container, PassContainerFragment.newInstance(pass.getId()))
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, PassDetailActivity.class);
                    intent.putExtra(PassDetailActivity.ARG_ITEM_ID, pass.getId());
                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(PassListActivity parent, boolean twoPane) {
            passes = new ArrayList<>();
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.pass_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Uri uri = Uri.parse(passes.get(position).getLogoImage());
            holder.logo.setImageURI(uri);
            holder.logoText.setText(passes.get(position).getLogoText());
            holder.description.setText(passes.get(position).getDescription());

            holder.itemView.setTag(passes.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return passes.size();
        }

        public void updatePasses(List<PassWallet> passes) {
            this.passes.clear();
            this.passes.addAll(passes);
            notifyDataSetChanged();
        }

        public void addPass(PassWallet pass){
            int idx = this.passes.indexOf(pass);
            if(idx >= 0){
                this.passes.set(idx, pass);
                notifyItemChanged(idx);
            }else{
                this.passes.add(0, pass);
                notifyItemInserted(0);
            }

        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView logo;
            final TextView logoText;
            final TextView description;

            ViewHolder(View view) {
                super(view);
                logo = view.findViewById(R.id.logo);
                logoText = view.findViewById(R.id.logo_text);
                description = view.findViewById(R.id.description);
            }
        }
    }
}
