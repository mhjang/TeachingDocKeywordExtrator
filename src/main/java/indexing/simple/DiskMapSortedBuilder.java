// BSD License (http://lemurproject.org/galago-license)
package indexing.simple;

import org.lemurproject.galago.core.index.disk.DiskBTreeWriter;
import org.lemurproject.galago.tupleflow.Parameters;

import java.io.IOException;

/**
 *
 * @author jfoley
 */
class DiskMapSortedBuilder {
	DiskBTreeWriter btree;
  Parameters opts;
  public DiskMapSortedBuilder(String path, Parameters opts) throws IOException {
    this.btree = new DiskBTreeWriter(path, opts);
    this.opts = opts;
  }
  
  public DiskMapSortedBuilder(String path) throws IOException {
    this(path, new Parameters());
  }
  
  /**
   * BTree requires keys put to be in ascending order.
   * @param key
   * @param value
   * @throws java.io.IOException
   * @see org.lemurproject.galago.core.index.disk.DiskBTreeWriter.add
   */
  public void put(byte[] key, byte[] value) throws IOException {
    btree.add(new DiskMapElement(key, value));
  }
  
  /** Call this when done adding keys!
   * @throws java.io.IOException
   */
  public void close() throws IOException {
    btree.close();
  }
}
