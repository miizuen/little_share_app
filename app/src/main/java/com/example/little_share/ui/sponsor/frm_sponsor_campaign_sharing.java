package com.example.little_share.ui.sponsor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.adapter.CampaignHistoryAdapter;
import com.example.little_share.data.models.CampaignHistoryModel;

import java.util.ArrayList;
import java.util.List;

public class frm_sponsor_campaign_sharing extends Fragment {

    private RecyclerView rvCampaignsHistory;
    private CampaignHistoryAdapter adapter;
    private List<CampaignHistoryModel> campaignList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sponsor_campaign_sharing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        loadCampaigns();
    }

    private void initViews(View view) {
        rvCampaignsHistory = view.findViewById(R.id.rvCampaignsHistory);
    }

    private void setupRecyclerView() {
        adapter = new CampaignHistoryAdapter();
        rvCampaignsHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCampaignsHistory.setAdapter(adapter);

        // Xử lý sự kiện click
        adapter.setClickListener(new CampaignHistoryAdapter.OnCampaignClickListener() {
            @Override
            public void onDetailClick(CampaignHistoryModel campaign, int position) {
                // Xử lý khi click button chi tiết
                Toast.makeText(getContext(),
                        "Chi tiết: " + campaign.getCampaignName(),
                        Toast.LENGTH_SHORT).show();

                // Có thể navigate đến màn hình chi tiết
                // NavController navController = Navigation.findNavController(requireView());
                // Bundle bundle = new Bundle();
                // bundle.putString("campaignName", campaign.getCampaignName());
                // navController.navigate(R.id.action_to_detail, bundle);
            }

            @Override
            public void onItemClick(CampaignHistoryModel campaign, int position) {
                // Xử lý khi click vào item
                Toast.makeText(getContext(),
                        "Đã chọn: " + campaign.getCampaignName(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCampaigns() {
                campaignList = new ArrayList<>();

        // Thêm dữ liệu mẫu
        campaignList.add(new CampaignHistoryModel(
                "Bữa cơm Nghĩa Tình",
                "Bệnh Viện K, Cơ sở 3 - Đống Đa, Hà Nội - Bếp Từ Tâm",
                "15/10 - 20/10",
                "20.000.000",
                R.drawable.logo_buacomnghiatinh,
                "Nấu ăn và dinh dưỡng"
        ));

        campaignList.add(new CampaignHistoryModel(
                "Mùa đông ấm áp",
                "Các tỉnh miền núi phía Bắc - Hà Giang, Lào Cai",
                "01/11 - 30/11",
                "15.500.000",
                R.drawable.img_quyengop_dochoi,
                "Tặng áo ấm"
        ));

        campaignList.add(new CampaignHistoryModel(
                "Ánh sáng học đường",
                "Các trường THCS vùng sâu - Cao Bằng, Bắc Kạn",
                "10/09 - 15/09",
                "30.000.000",
                R.drawable.img_nauanchoem,
                "Xây dựng lớp học"
        ));

        campaignList.add(new CampaignHistoryModel(
                "Nước sạch cho em",
                "Xã Phiêng Luông, Mộc Châu - Sơn La",
                "05/08 - 25/08",
                "45.000.000",
                R.drawable.logo_buacomnghiatinh,
                "Xây bể chứa nước"
        ));

        campaignList.add(new CampaignHistoryModel(
                "Học bổng tương lai",
                "Học sinh nghèo vượt khó toàn quốc",
                "01/07 - 31/07",
                "25.000.000",
                R.drawable.logo_buacomnghiatinh,
                "Trao học bổng"
        ));

        campaignList.add(new CampaignHistoryModel(
                "Trung thu yêu thương",
                "Trẻ em mồ côi các tỉnh Miền Trung",
                "15/08 - 30/08",
                "18.000.000",
                R.drawable.img_quyengop_dochoi,
                "Tặng quà Trung thu"
        ));


        adapter.setCampaignList(campaignList);
    }
}