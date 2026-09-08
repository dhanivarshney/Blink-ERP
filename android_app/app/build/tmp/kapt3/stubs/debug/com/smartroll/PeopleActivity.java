package com.smartroll;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001:\u0001\u000eB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\t\u001a\u00020\nH\u0002J\u0012\u0010\u000b\u001a\u00020\n2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u0014R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/smartroll/PeopleActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "loader", "Landroid/view/View;", "repository", "Lcom/smartroll/repository/MainRepository;", "rvUsers", "Landroidx/recyclerview/widget/RecyclerView;", "loadUsers", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "UserAdapter", "app_debug"})
public final class PeopleActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.smartroll.repository.MainRepository repository;
    private androidx.recyclerview.widget.RecyclerView rvUsers;
    private android.view.View loader;
    
    public PeopleActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void loadUsers() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\u0011B\u0013\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\u0002\u0010\u0006J\b\u0010\u0007\u001a\u00020\bH\u0016J\u0018\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u00022\u0006\u0010\f\u001a\u00020\bH\u0016J\u0018\u0010\r\u001a\u00020\u00022\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\bH\u0016R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/smartroll/PeopleActivity$UserAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/smartroll/PeopleActivity$UserAdapter$VH;", "users", "", "Lcom/smartroll/db/UserEntity;", "(Ljava/util/List;)V", "getItemCount", "", "onBindViewHolder", "", "h", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "VH", "app_debug"})
    public static final class UserAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.smartroll.PeopleActivity.UserAdapter.VH> {
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.smartroll.db.UserEntity> users = null;
        
        public UserAdapter(@org.jetbrains.annotations.NotNull()
        java.util.List<com.smartroll.db.UserEntity> users) {
            super();
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public com.smartroll.PeopleActivity.UserAdapter.VH onCreateViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.ViewGroup parent, int viewType) {
            return null;
        }
        
        @java.lang.Override()
        public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
        com.smartroll.PeopleActivity.UserAdapter.VH h, int position) {
        }
        
        @java.lang.Override()
        public int getItemCount() {
            return 0;
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\t\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0019\u0010\u0005\u001a\n \u0006*\u0004\u0018\u00010\u00030\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0019\u0010\t\u001a\n \u0006*\u0004\u0018\u00010\n0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0019\u0010\r\u001a\n \u0006*\u0004\u0018\u00010\n0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\fR\u0019\u0010\u000f\u001a\n \u0006*\u0004\u0018\u00010\n0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\fR\u0019\u0010\u0011\u001a\n \u0006*\u0004\u0018\u00010\n0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\f\u00a8\u0006\u0013"}, d2 = {"Lcom/smartroll/PeopleActivity$UserAdapter$VH;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "v", "Landroid/view/View;", "(Landroid/view/View;)V", "btnDelete", "kotlin.jvm.PlatformType", "getBtnDelete", "()Landroid/view/View;", "icon", "Landroid/widget/TextView;", "getIcon", "()Landroid/widget/TextView;", "subtitle", "getSubtitle", "tag", "getTag", "title", "getTitle", "app_debug"})
        public static final class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            private final android.widget.TextView icon = null;
            private final android.widget.TextView title = null;
            private final android.widget.TextView subtitle = null;
            private final android.widget.TextView tag = null;
            private final android.view.View btnDelete = null;
            
            public VH(@org.jetbrains.annotations.NotNull()
            android.view.View v) {
                super(null);
            }
            
            public final android.widget.TextView getIcon() {
                return null;
            }
            
            public final android.widget.TextView getTitle() {
                return null;
            }
            
            public final android.widget.TextView getSubtitle() {
                return null;
            }
            
            public final android.widget.TextView getTag() {
                return null;
            }
            
            public final android.view.View getBtnDelete() {
                return null;
            }
        }
    }
}