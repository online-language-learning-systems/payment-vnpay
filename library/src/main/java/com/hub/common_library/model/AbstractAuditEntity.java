package com.hub.common_library.model;

import com.hub.common_library.model.listener.CustomAuditingEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.OffsetDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(CustomAuditingEntityListener.class)
public class AbstractAuditEntity {

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @UpdateTimestamp
    @Column(name = "last_modified_on")
    private OffsetDateTime lastModifiedOn;

}
