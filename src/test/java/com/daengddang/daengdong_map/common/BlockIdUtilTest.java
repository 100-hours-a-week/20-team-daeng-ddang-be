package com.daengddang.daengdong_map.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BlockIdUtilTest {

    @Test
    void toBlockXAndY_floorByBlockSize() {
        double lat = BlockIdUtil.BLOCK_SIZE * 1.9;
        double lng = BlockIdUtil.BLOCK_SIZE * 2.1;

        assertThat(BlockIdUtil.toBlockX(lat)).isEqualTo(1);
        assertThat(BlockIdUtil.toBlockY(lng)).isEqualTo(2);
    }

    @Test
    void toBlockId_formatsWithFixedScale() {
        String blockId = BlockIdUtil.toBlockId(1, 2);

        assertThat(blockId).isEqualTo("P_0.000720_0.001440");
    }

    @Test
    void toBlockX_handlesNegativeCoordinates() {
        double lat = -0.00010;

        assertThat(BlockIdUtil.toBlockX(lat)).isEqualTo(-1);
    }
}
