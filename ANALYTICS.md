# Campaign Analytics Feature

## Overview

The Campaign Analytics feature provides comprehensive insights into campaign performance and usage patterns. It automatically tracks every campaign application and provides detailed reporting through both REST APIs and a web-based admin dashboard.

## Key Features

### 📊 Automatic Usage Tracking
- Every campaign application is automatically recorded
- Tracks original cart total, discount amount, final total, and timestamp
- Supports transaction ID for detailed audit trails
- No performance impact on campaign evaluation process

### 🔍 Analytics & Reporting
- **Total Applications**: Count of how many times each campaign was used
- **Total Discounts**: Sum of all discount amounts provided by each campaign
- **Average Discount**: Average discount amount per application
- **Date Range Filtering**: Analyze performance over specific time periods
- **Campaign Comparison**: Compare performance across different campaigns

### 🌐 REST API Endpoints

All analytics endpoints are secured and require `ADMIN` or `CAMPAIGN_MANAGER` role.

#### Get Analytics for Date Range
```http
GET /api/v1/analytics/campaigns?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
```

#### Get Last 30 Days Analytics
```http
GET /api/v1/analytics/campaigns/last30days
```

#### Get Individual Campaign Statistics
```http
GET /api/v1/analytics/campaigns/{campaignId}
```

#### Get Daily Statistics for Campaign
```http
GET /api/v1/analytics/campaigns/{campaignId}/daily?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
```

### 💻 Admin Dashboard

Access the analytics dashboard at `/admin/analytics` (requires authentication).

**Features:**
- Summary cards showing key metrics
- Date range filtering
- Detailed campaign performance table
- Direct links to edit campaigns
- Responsive design matching existing admin UI

## Usage Examples

### REST API Example Response
```json
{
  "campaignStatistics": [
    {
      "campaignId": 1,
      "campaignName": "Summer Sale 20%",
      "totalApplications": 150,
      "totalDiscountAmount": 2500.00,
      "averageDiscountAmount": 16.67,
      "campaignType": "BASKET_TOTAL_PERCENTAGE_DISCOUNT",
      "active": true
    }
  ],
  "totalDiscountsProvided": 2500.00,
  "totalCampaignApplications": 150,
  "dateRangeStart": "2024-01-01T00:00:00",
  "dateRangeEnd": "2024-01-31T23:59:59"
}
```

### Using Transaction ID in Cart Evaluation
```http
POST /api/v1/campaigns/evaluate?transactionId=TXN-12345
Content-Type: application/json

{
  "items": [
    {
      "productId": "PROD-001",
      "quantity": 2,
      "unitPrice": 50.00
    }
  ]
}
```

## Technical Implementation

### Database Schema

#### campaign_usage Table
- `id`: Primary key
- `campaign_id`: Foreign key to campaigns table
- `original_cart_total`: Cart total before discounts
- `discount_amount`: Amount of discount applied
- `final_cart_total`: Cart total after discount
- `applied_at`: Timestamp when campaign was applied
- `transaction_id`: Optional transaction identifier
- `metadata`: Additional JSON metadata

### Service Layer

#### CampaignAnalyticsService
- `recordCampaignUsage()`: Records individual campaign applications
- `getCampaignAnalytics()`: Returns comprehensive analytics for date range
- `getCampaignStatistics()`: Returns statistics for specific campaign
- `getDailyStatistics()`: Returns daily breakdown for campaign

### Integration with Campaign Engine

The analytics feature is seamlessly integrated into the existing campaign evaluation process:

1. When `CampaignService.evaluateCart()` is called, it automatically records usage data
2. Recording happens asynchronously to avoid impacting performance
3. Failed analytics recording doesn't affect campaign evaluation
4. Supports both anonymous and transaction-identified applications

## Configuration

No additional configuration is required. The feature works out-of-the-box with:

- Existing database configuration
- Current security setup
- Present admin panel authentication

## Benefits

1. **Data-Driven Decisions**: Make informed choices about campaign effectiveness
2. **ROI Analysis**: Understand the financial impact of each campaign
3. **Usage Patterns**: Identify peak campaign usage times and trends
4. **Campaign Optimization**: Adjust campaigns based on real performance data
5. **Audit Trail**: Complete history of all campaign applications
6. **Business Intelligence**: Export data for further analysis and reporting

## Future Enhancements

The analytics foundation supports future enhancements such as:
- Campaign A/B testing analytics
- Customer segment analysis
- Seasonal trend reporting
- Automated campaign optimization recommendations
- Integration with external BI tools