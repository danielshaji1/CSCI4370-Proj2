package uga.menik.csx370.models;

/** A private collection with an aggregate count for the collection list. */
public record SavedCollection(long collectionId, String name, String description, String createdAt, int postCount) {}
