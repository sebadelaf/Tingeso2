package com.tingeso.rackpistaservice.Repository;

import com.tingeso.rackpistaservice.Entity.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<ReservaEntity, Long>  {
}
