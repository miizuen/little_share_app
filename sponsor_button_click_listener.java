// Code cho sự kiện khi bấm vào nút "TÀI TRỢ NGAY" (btnSponsor.setOnClickListener)
// Đặt code này vào adapter SponsorCampaignNeedAdapter trong method setupClickListeners

btnDonate.setOnClickListener(v -> {
    if(listener != null){
        listener.onDonateClick(campaign);
    }
});

// Hoặc nếu bạn muốn xử lý trực tiếp trong adapter:
btnDonate.setOnClickListener(v -> {
    // Lấy thông tin campaign
    String campaignId = campaign.getId();
    String campaignName = campaign.getName();
    double targetBudget = campaign.getTargetBudget();
    double currentBudget = campaign.getCurrentBudget();
    
    // Tạo Intent để chuyển đến trang donation form
    Intent intent = new Intent(context, activity_sponsor_donation_form.class);
    intent.putExtra("campaign_id", campaignId);
    intent.putExtra("campaign_name", campaignName);
    intent.putExtra("organization_name", campaign.getOrganizationName());
    intent.putExtra("target_budget", targetBudget);
    intent.putExtra("current_budget", currentBudget);
    
    // Start activity
    context.startActivity(intent);
});