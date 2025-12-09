package com.example.little_share.ui.sponsor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.little_share.R;
import com.example.little_share.adapter.sponsor.CampaignSponsorAdapter;
import com.example.little_share.data.models.sponsor.CampaignSponsorModel;

import java.util.ArrayList;
import java.util.List;

public class frm_sponsor_home extends Fragment {

    private RecyclerView rvSponsoredCampaigns;
    private RecyclerView rvNeedSponsorCampaigns;
    private TextView tvEmptySponsored;

    private CampaignSponsorAdapter sponsoredAdapter;
    private CampaignSponsorAdapter needSponsorAdapter;

    private List<CampaignSponsorModel> sponsoredList;
    private List<CampaignSponsorModel> needSponsorList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sponsor_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerViews();
        loadData();
    }

    private void initViews(View view) {
        rvSponsoredCampaigns = view.findViewById(R.id.rvSponsoredCampaigns);
        rvNeedSponsorCampaigns = view.findViewById(R.id.rvNeedSponsorCampaigns);
        tvEmptySponsored = view.findViewById(R.id.tvEmptySponsored);
    }

    private void setupRecyclerViews() {
        // Setup RecyclerView cho chiến dịch đang tài trợ
        sponsoredAdapter = new CampaignSponsorAdapter();
        rvSponsoredCampaigns.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSponsoredCampaigns.setAdapter(sponsoredAdapter);

        sponsoredAdapter.setClickListener(new CampaignSponsorAdapter.OnCampaignClickListener() {
            @Override
            public void onButtonClick(CampaignSponsorModel campaign, int position) {
                Toast.makeText(getContext(),
                        "Chi tiết: " + campaign.getCampaignName(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemClick(CampaignSponsorModel campaign, int position) {
                Toast.makeText(getContext(),
                        "Đã chọn: " + campaign.getCampaignName(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Setup RecyclerView cho chiến dịch cần tài trợ
        needSponsorAdapter = new CampaignSponsorAdapter();
        rvNeedSponsorCampaigns.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNeedSponsorCampaigns.setAdapter(needSponsorAdapter);

        needSponsorAdapter.setClickListener(new CampaignSponsorAdapter.OnCampaignClickListener() {
            @Override
            public void onButtonClick(CampaignSponsorModel campaign, int position) {
                Toast.makeText(getContext(),
                        "Tài trợ cho: " + campaign.getCampaignName(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemClick(CampaignSponsorModel campaign, int position) {
                Toast.makeText(getContext(),
                        "Xem chi tiết: " + campaign.getCampaignName(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        loadSponsoredCampaigns();
        loadNeedSponsorCampaigns();
    }

    private void loadSponsoredCampaigns() {
        sponsoredList = new ArrayList<>();

        // Thêm dữ liệu mẫu - constructor cho chiến dịch đang tài trợ
        sponsoredList.add(new CampaignSponsorModel(
                "Nấu ăn cho em",
                "Nấu ăn và dinh dưỡng",
                "#FF6F00",
                "Đang chạy",
                "#1B6A07",
                "Nhóm tình nguyện niềm tin",
                "Cao Bằng",
                "12/10/2024 - 30/12/2024",
                "5.000.000",
                73,
                R.drawable.img_nauanchoem
        ));

        sponsoredList.add(new CampaignSponsorModel(
                "Ánh sáng học đường",
                "Giáo dục",
                "#2196F3",
                "Đang chạy",
                "#1B6A07",
                "Quỹ Vì Trẻ Em",
                "Lào Cai",
                "15/11/2024 - 28/02/2025",
                "10.000.000",
                85,
                R.drawable.img_nauanchoem
        ));

        sponsoredList.add(new CampaignSponsorModel(
                "Mùa đông ấm áp",
                "Y tế & Sức khỏe",
                "#E91E63",
                "Hoàn thành",
                "#22C55E",
                "Hội Chữ Thập Đỏ",
                "Hà Giang",
                "01/11/2024 - 30/11/2024",
                "8.000.000",
                100,
                R.drawable.img_nauanchoem
        ));

        sponsoredAdapter.setCampaignList(sponsoredList);

        // Hiển thị/ẩn thông báo empty
        if (sponsoredList.isEmpty()) {
            tvEmptySponsored.setVisibility(View.VISIBLE);
            rvSponsoredCampaigns.setVisibility(View.GONE);
        } else {
            tvEmptySponsored.setVisibility(View.GONE);
            rvSponsoredCampaigns.setVisibility(View.VISIBLE);
        }
    }

    private void loadNeedSponsorCampaigns() {
        needSponsorList = new ArrayList<>();

        // Thêm dữ liệu mẫu - constructor cho chiến dịch cần tài trợ
        needSponsorList.add(new CampaignSponsorModel(
                "Quyên góp đồ chơi cho trẻ em nghèo",
                "Giáo dục",
                "#FF6F00",
                "Nhóm tình nguyện Niềm Tin",
                "Cao Bằng",
                "12/10/2025 - 30/12/2025",
                "50M",
                "1000",
                "6 tháng",
                "50M",
                "100M",
                50,
                R.drawable.img_quyengop_dochoi
        ));

        needSponsorList.add(new CampaignSponsorModel(
                "Xây dựng trường học vùng cao",
                "Giáo dục",
                "#2196F3",
                "Hội Khuyến Học",
                "Sơn La",
                "01/12/2024 - 30/06/2025",
                "200M",
                "500",
                "7 tháng",
                "80M",
                "200M",
                40,
                R.drawable.img_quyengop_dochoi
        ));

        needSponsorList.add(new CampaignSponsorModel(
                "Trao học bổng cho học sinh nghèo",
                "Giáo dục",
                "#9C27B0",
                "Quỹ Vì Trẻ Em",
                "Điện Biên",
                "15/11/2024 - 15/03/2025",
                "30M",
                "200",
                "4 tháng",
                "18M",
                "30M",
                60,
                R.drawable.img_quyengop_dochoi
        ));

        needSponsorList.add(new CampaignSponsorModel(
                "Nước sạch cho bà con vùng núi",
                "Y tế & Sức khỏe",
                "#E91E63",
                "Hội Chữ Thập Đỏ",
                "Lai Châu",
                "20/11/2024 - 30/04/2025",
                "150M",
                "800",
                "5 tháng",
                "45M",
                "150M",
                30,
                R.drawable.img_quyengop_dochoi
        ));

        needSponsorAdapter.setCampaignList(needSponsorList);
    }
}