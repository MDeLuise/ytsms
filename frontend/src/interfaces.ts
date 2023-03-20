export interface Subscription {
    id: Number,
    channelId: string,
    channelName?: string,
    userId: Number,
    channelThumbnailLink?: string,
}

export interface Video {
    id: Number,
    title?: string,
    publishedAt?: Date,
    channelName?: string,
    channelId: string,
    thumbnailLink?: string,
    view?: Number,
    secondsDuration?: Number,
}

export interface ScraperStatus {
    lastScrape: Date,
    scraping: boolean,
    isLastFailed: boolean,
}