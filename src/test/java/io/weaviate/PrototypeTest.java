package io.weaviate;

import java.util.List;

import io.weaviate.collections.Collection;
import io.weaviate.collections.CollectionsClient;
import io.weaviate.collections.Property;
import io.weaviate.data.CollectionClient;
import io.weaviate.data.WeaviateObject;
import io.weaviate.data.query.Hybrid.Fusion;
import lombok.AllArgsConstructor;

@io.weaviate.annotations.Collection("Songs")
@AllArgsConstructor
class Song extends WeaviateObject {
  public final String artist;
  public final String title;
  public final String lyrics;
}

class PrototypeTest {
  void test() {
    WeaviateClient client = WeaviateConnect.toLocal();
    client.collections.create(Song.class);

    client.collections.create("Songs", opt -> opt
        .properties(
            Property.text("title"),
            Property.text("lyrics")));

    Collection definition = client.collections.get("Songs");
    System.out.println("Created collection " + definition.name);

    CollectionClient<Song> songs = client.useCollection();

    Song firstSong = new Song("Blackpink Rose", "APT", "apateu apateu apateu");
    songs.data.insert(firstSong);

    List<Song> likedSongs = List.of(
        new Song("Eminem", "Stan", "stan stan stan"),
        new Song("Michael Jackson", "Dangerous", "hee-hee"),
        new Song("Zaz", "Je veux", "pa-para-pa-pa-pada"),
        new Song("Boney M", "Ma Baker", "ma-ma-ma-ma"));
    songs.data.insertMany(likedSongs);

    List<Song> bm25 = songs.query.bm25(
        "look, I was gonna go easy on you not to hurt your feelings",
        opt -> opt
            .limit(1));

    List<Song> hybridResult = songs.query.hybrid(
        "A person who is not my friend",
        opt -> opt
            .returnProperties("artist", "title", "lyrics")
            .fusion(Fusion.RANKED)
            .alpha(.8f));
  }
}
