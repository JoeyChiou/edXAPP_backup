package tw.openedu.www.view.adapters;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import tw.openedu.www.R;
import tw.openedu.www.core.IEdxEnvironment;
import tw.openedu.www.logger.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public abstract class BaseListAdapter<T> extends ArrayAdapter<T> implements OnItemClickListener {
    public static interface PaginationHandler{
        void loadMoreRecord(IPagination pagination);
    }

    static class RowType {
        static final int DATA_ROW = 0;
        static final int MORE_BUTTON = 1;
    }

    // constants that define selection state of list rows
    public static final int STATE_NOT_SELECTED = 0;
    public static final int STATE_SELECTED = 1;
    private final int layoutResource;
    private final List<T> objects;

    private SparseIntArray selection = new SparseIntArray(); 
    public static final long MIN_CLICK_INTERVAL = 1000; //in millis
    protected final Logger logger = new Logger(getClass().getName());

    //simple way to avoid click on same row too quickly.
    protected LastClickedEvent lastClickedEvent = new LastClickedEvent();

    protected IEdxEnvironment environment;

    private IPagination pagination = new BasePagination(IPagination.DEFAULT_PAGE_SIZE);
    private PaginationHandler paginationHandler;


    public IPagination getPagination() {
        return pagination;
    }

    public void setPaginationHandler(PaginationHandler paginationHandler) {
        this.paginationHandler = paginationHandler;
    }

    public BaseListAdapter(Context context, int layoutResourceId, IEdxEnvironment environment) {
        this(context, layoutResourceId, environment, new ArrayList<T>());
    }

    private BaseListAdapter(Context context, int layoutResourceId, IEdxEnvironment environment, List<T> list) {
        super(context, layoutResourceId, list);
        layoutResource = layoutResourceId;
        this.environment = environment;
        this.objects = list;
    }
    
    /**
     * Selects the list row at specified position.
     * @param position
     */
    public void select(int position) {
        selection.put(position, STATE_SELECTED);
    }
    
    /**
     * De-selects the list row at specified position.
     * @param position
     */
    public void unselect(int position) {
        selection.put(position, STATE_NOT_SELECTED);
    }
    
    /**
     * Selects all the items in this adapter.
     */
    public void selectAll() {
        for (int i=0; i<getCount(); i++) {
            select(i);
        }
    }
    
    /**
     * De-selects all the items from this adapter.
     */
    public void unselectAll() {
        selection.clear();
    }
    
    /**
     * Returns true if list row at specified position is selected, false otherwise.
     * @param position
     * @return
     */
    public boolean isSelected(int position) {
        Integer val = selection.get(position);
        if (val == null)
            return false;
        return (val == STATE_SELECTED);
    }
    
    /**
     * Returns list of selected items.
     * @return
     */
    public ArrayList<T> getSelectedItems() {
        ArrayList<T> selectedItems = new ArrayList<T>();
        for (int i=0; i<getCount(); i++) {
            if (isSelected(i)) {
                selectedItems.add(getItem(i));
            }
        }
        return selectedItems;
    }
    
    /**
     * Clears existing items from the adapter and sets given list as the data. 
     * If null is provided, this method clears the existing values. 
     * This avoids null value errors. 
     * @param newItems
     */
    public void setItems(List<T> newItems) {
        clear();

        if (newItems != null) {
            addPage(newItems, false);
        }
    }

    public void addPage(Collection<? extends T> collection, boolean hasMore) {
         super.addAll(collection);
         pagination.addPage(collection.size());
         pagination.setHasMorePages(hasMore);
    }

    public void replace(T item, int position) {
        objects.set(position, item);
        notifyDataSetChanged();
    }

    public int getCount(){
        if ( pagination.mayHasMorePages() ) {
            return super.getCount() + 1;
        } else {
            return super.getCount() ;
        }
    }
    /**
     * Clears all items from this adapter.
     */
    public void clear() {
        super.clear();
        selection.clear();
        lastClickedEvent = new LastClickedEvent();
        pagination.clear();
    }

    public int getItemViewType(int position) {
        if ( pagination.mayHasMorePages() ) {
            return position == getCount() - 1 ?  RowType.MORE_BUTTON : RowType.DATA_ROW;
        } else {
            return RowType.DATA_ROW;
        }
    }

    public int getViewTypeCount() {
         return pagination.mayHasMorePages() ? 2 : 1;
    }


    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup adapter) {
        try {
            int rowType = getItemViewType(position);

            if ( rowType == RowType.MORE_BUTTON ){

                if (convertView == null) {
                    // create list row
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.show_more_button_row, adapter, false);

                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (paginationHandler != null )
                                paginationHandler.loadMoreRecord(pagination);
                        }
                    });
                }
                return convertView;
            }


            if (convertView == null) {
                // create list row
                convertView = LayoutInflater.from(getContext()).inflate(layoutResource, adapter, false);
                
                // apply a tag to this list row
                BaseViewHolder tag = getTag(convertView);
                convertView.setTag(tag);
            }
    
            // get the tag for this list row
            BaseViewHolder tag = (BaseViewHolder) convertView.getTag();
            // put position into the holder object
            tag.position = position;
            
            // get model data for this list row
            T model = getItem(position);
            
            // now render data for this list row
            render(tag, model);
            
            return convertView;
        } catch(Exception ex) {
            logger.error(ex);
        }
        
        return convertView;
    }
    
    

    /**
     * Sub-class should override this method to render model's data to ViewHolder tag.
     * @param tag
     * @param model
     */
    public abstract void render(BaseViewHolder tag, T model);

    /**
     * Sub-class should override this method and return ViewHolder tag that
     * is to be applied to the given convertView.
     * @param convertView
     * @return
     */
    public abstract BaseViewHolder getTag(View convertView);

    /**
     * help method to avoid quick click on the same row
     * @param position  the row it clicks
     * @return  <code>true</code> click can pass through <code>false</code> click on
     * the same row moment ago
     */
    protected final boolean testClickAcceptable(int position){
        final int acceptableElapseTime = 500;
        if ( lastClickedEvent.position != position ){
            lastClickedEvent.position = position;
            lastClickedEvent.timestamp = new Date().getTime();
            return true;
        } else {
            long previousClickTime = lastClickedEvent.timestamp;
            lastClickedEvent.timestamp = new Date().getTime();
            return  lastClickedEvent.timestamp - previousClickTime > acceptableElapseTime;
        }
    }

    /**
     * Base class for ViewHolders in individual adapters.
     */
    public static class BaseViewHolder {
        public int position;
        public String videoId;
    }

    /**
     * remember the last click for list view
     */
    private  class LastClickedEvent {
        public int position;
        public long timestamp;
    }
}
