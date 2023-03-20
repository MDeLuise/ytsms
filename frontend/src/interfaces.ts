export interface subscription {
    id: Number,
    channelId: string,
    channelName?: string,
    userId: Number,
    channelThumbnailLink?: string,
}

export interface video {
    id: Number,
    title?: string,
    publishedAt?: Date,
    channelName?: string,
    channelId: string,
    thumbnailLink?: string,
    view?: Number,
    secondsDuration?: Number,
}