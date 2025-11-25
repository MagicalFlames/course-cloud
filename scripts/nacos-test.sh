#!/bin/bash

# Nacos 服务发现测试脚本
# 测试服务注册和发现功能

echo "=========================================="
echo "  Nacos 服务发现测试"
echo "=========================================="

NACOS_HOST=${NACOS_HOST:-localhost}
NACOS_PORT=${NACOS_PORT:-8848}
CATALOG_HOST=${CATALOG_HOST:-localhost}
ENROLLMENT_HOST=${ENROLLMENT_HOST:-localhost}

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 等待服务启动
wait_for_service() {
    local url=$1
    local name=$2
    local max_attempts=30
    local attempt=1

    echo -n "等待 $name 启动..."
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            echo -e " ${GREEN}✓${NC}"
            return 0
        fi
        sleep 2
        attempt=$((attempt + 1))
    done
    echo -e " ${RED}✗${NC}"
    return 1
}

# 测试 Nacos 控制台
test_nacos_console() {
    echo ""
    echo "1. 测试 Nacos 控制台"
    echo "-------------------------------------------"

    response=$(curl -s "http://${NACOS_HOST}:${NACOS_PORT}/nacos/v1/console/health/readiness")
    if [ "$response" == "OK" ]; then
        echo -e "   Nacos 控制台: ${GREEN}正常${NC}"
        echo "   访问地址: http://${NACOS_HOST}:${NACOS_PORT}/nacos"
        return 0
    else
        echo -e "   Nacos 控制台: ${RED}异常${NC}"
        return 1
    fi
}

# 测试服务注册
test_service_registration() {
    echo ""
    echo "2. 测试服务注册"
    echo "-------------------------------------------"

    # 获取已注册的服务列表
    services=$(curl -s "http://${NACOS_HOST}:${NACOS_PORT}/nacos/v1/ns/service/list?pageNo=1&pageSize=10")

    echo "   已注册服务列表:"
    echo "$services" | python3 -m json.tool 2>/dev/null || echo "$services"

    # 检查 catalog-service
    if echo "$services" | grep -q "catalog-service"; then
        echo -e "   catalog-service: ${GREEN}已注册${NC}"
    else
        echo -e "   catalog-service: ${YELLOW}未注册${NC}"
    fi

    # 检查 enrollment-service
    if echo "$services" | grep -q "enrollment-service"; then
        echo -e "   enrollment-service: ${GREEN}已注册${NC}"
    else
        echo -e "   enrollment-service: ${YELLOW}未注册${NC}"
    fi
}

# 测试服务实例
test_service_instances() {
    echo ""
    echo "3. 测试服务实例详情"
    echo "-------------------------------------------"

    for service in "catalog-service" "enrollment-service"; do
        echo ""
        echo "   $service 实例:"
        instances=$(curl -s "http://${NACOS_HOST}:${NACOS_PORT}/nacos/v1/ns/instance/list?serviceName=$service")

        if echo "$instances" | grep -q "\"hosts\":\[\]"; then
            echo -e "   ${YELLOW}无可用实例${NC}"
        else
            echo "$instances" | python3 -m json.tool 2>/dev/null || echo "$instances"
        fi
    done
}

# 测试健康检查端点
test_health_endpoints() {
    echo ""
    echo "4. 测试服务健康检查端点"
    echo "-------------------------------------------"

    # Catalog service health
    echo "   catalog-service 健康检查:"
    health=$(curl -s "http://${CATALOG_HOST}:8081/actuator/health")
    status=$(echo "$health" | grep -o '"status":"[^"]*"' | head -1)
    if echo "$status" | grep -q "UP"; then
        echo -e "   状态: ${GREEN}UP${NC}"
    else
        echo -e "   状态: ${RED}$status${NC}"
    fi

    # Enrollment service health
    echo "   enrollment-service 健康检查:"
    health=$(curl -s "http://${ENROLLMENT_HOST}:8082/actuator/health")
    status=$(echo "$health" | grep -o '"status":"[^"]*"' | head -1)
    if echo "$status" | grep -q "UP"; then
        echo -e "   状态: ${GREEN}UP${NC}"
    else
        echo -e "   状态: ${RED}$status${NC}"
    fi
}

# 测试服务间调用（通过服务发现）
test_service_communication() {
    echo ""
    echo "5. 测试服务间调用（通过 Nacos 服务发现）"
    echo "-------------------------------------------"

    # 创建测试课程
    echo "   创建测试课程..."
    course_response=$(curl -s -X POST "http://${CATALOG_HOST}:8081/api/courses" \
        -H "Content-Type: application/json" \
        -d '{
            "code": "NACOS001",
            "title": "Nacos 测试课程",
            "instructor": {
                "id": "T001",
                "name": "测试教授",
                "email": "test@example.edu.cn"
            },
            "scheduleSlot": {
                "dayOfWeek": "MONDAY",
                "startTime": "08:00",
                "endTime": "10:00",
                "expectedAttendance": 50
            },
            "capacity": 60,
            "enrolledCount": 0
        }')

    course_id=$(echo "$course_response" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)

    if [ -n "$course_id" ]; then
        echo -e "   课程创建: ${GREEN}成功${NC} (ID: $course_id)"

        # 创建测试学生
        echo "   创建测试学生..."
        student_response=$(curl -s -X POST "http://${ENROLLMENT_HOST}:8082/api/students" \
            -H "Content-Type: application/json" \
            -d '{
                "studentId": "NACOS001",
                "name": "Nacos测试学生",
                "major": "计算机科学",
                "grade": 2024,
                "email": "nacos-test@example.edu.cn"
            }')

        if echo "$student_response" | grep -q '"success":true'; then
            echo -e "   学生创建: ${GREEN}成功${NC}"

            # 测试选课（这会通过 Nacos 服务发现调用 catalog-service）
            echo "   测试选课（通过服务发现）..."
            enroll_response=$(curl -s -X POST "http://${ENROLLMENT_HOST}:8082/api/enrollments" \
                -H "Content-Type: application/json" \
                -d "{
                    \"studentId\": \"NACOS001\",
                    \"courseId\": \"$course_id\"
                }")

            if echo "$enroll_response" | grep -q '"success":true'; then
                echo -e "   选课测试: ${GREEN}成功${NC}"
                echo -e "   ${GREEN}服务发现功能正常工作！${NC}"
            else
                echo -e "   选课测试: ${RED}失败${NC}"
                echo "   响应: $enroll_response"
            fi
        else
            echo -e "   学生创建: ${RED}失败${NC}"
        fi
    else
        echo -e "   课程创建: ${RED}失败${NC}"
        echo "   响应: $course_response"
    fi
}

# 主函数
main() {
    echo ""

    # 等待服务启动
    wait_for_service "http://${NACOS_HOST}:${NACOS_PORT}/nacos" "Nacos" || exit 1
    wait_for_service "http://${CATALOG_HOST}:8081/actuator/health" "catalog-service" || exit 1
    wait_for_service "http://${ENROLLMENT_HOST}:8082/actuator/health" "enrollment-service" || exit 1

    # 运行测试
    test_nacos_console
    test_service_registration
    test_service_instances
    test_health_endpoints
    test_service_communication

    echo ""
    echo "=========================================="
    echo "  测试完成"
    echo "=========================================="
    echo ""
    echo "Nacos 控制台: http://${NACOS_HOST}:${NACOS_PORT}/nacos"
    echo "默认账号: nacos / nacos"
    echo ""
}

main "$@"
