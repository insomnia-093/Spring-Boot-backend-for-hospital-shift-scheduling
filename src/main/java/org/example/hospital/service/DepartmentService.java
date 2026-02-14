package org.example.hospital.service;

import java.util.List;
import java.util.stream.Collectors;
import org.example.hospital.domain.Department;
import org.example.hospital.dto.DepartmentRequest;
import org.example.hospital.dto.DepartmentResponse;
import org.example.hospital.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        logger.info("创建科室: {}", request.getName());
        Department department = new Department(request.getName(), request.getDescription());
        Department saved = departmentRepository.save(department);
        logger.info("科室创建成功: ID={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        logger.debug("查询所有科室");
        return departmentRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        logger.debug("查询科室: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("科室不存在: {}", id);
                    return new IllegalArgumentException("Department not found");
                });
        return toResponse(department);
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        logger.info("更新科室: ID={}, 新名称={}", id, request.getName());
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("科室不存在: {}", id);
                    return new IllegalArgumentException("Department not found");
                });
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        logger.info("科室更新成功: {}", id);
        return toResponse(department);
    }

    @Transactional
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Department not found");
        }
        departmentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Department requireById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
    }

    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(department.getId(), department.getName(), department.getDescription());
    }
}
