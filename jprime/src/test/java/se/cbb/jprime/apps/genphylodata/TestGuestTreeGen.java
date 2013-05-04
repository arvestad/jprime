package se.cbb.jprime.apps.genphylodata;

import java.net.URL;

import org.junit.Test;

public class TestGuestTreeGen {

	@Test
	public void test() throws Exception {
		URL url = this.getClass().getResource("/phylogenetics/hybrid_graph_w_extinctions.gml");
		GuestTreeGen g = new GuestTreeGen();
		g.main(new String[] { "-q", "-hybrid", "0.05", "2", "2", url.getPath(), "0", "0", "0"} );
	}
}
