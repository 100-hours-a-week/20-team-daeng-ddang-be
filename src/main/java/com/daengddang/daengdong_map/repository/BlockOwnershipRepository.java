package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.block.BlockOwnership;
import com.daengddang.daengdong_map.domain.dog.Dog;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockOwnershipRepository extends JpaRepository<BlockOwnership, Long> {

    List<BlockOwnership> findAllByDog(Dog dog);

    List<BlockOwnership> findAllByIdIn(Collection<Long> blockIds);
}
