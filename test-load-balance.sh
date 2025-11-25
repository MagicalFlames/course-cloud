#!/bin/bash

# 测试负载均衡效果的脚本

echo "======================================"
echo "测试负载均衡效果"
echo "======================================"

# 首先测试是否能访问服务
echo -e "\n1. 测试服务是否可访问..."
curl -s http://localhost:8082/actuator/health | jq . || echo "enrollment-service未就绪"

echo -e "\n2. 获取所有课程（用于获取课程ID）..."
curl -s http://localhost:8082/api/students || echo "无法获取学生列表"

# 测试多次选课请求，观察负载均衡
echo -e "\n3. 发送多次请求测试负载均衡（查看服务日志）..."
echo "请在另一个终端运行以下命令查看日志："
echo "docker logs -f catalog-service-1"
echo "docker logs -f catalog-service-2"
echo "docker logs -f catalog-service-3"
echo ""

for i in {1..10}
do
    echo "请求 #$i"
    # 假设有一个课程ID，这里需要替换成实际的课程ID
    curl -s http://localhost:8082/api/enrollments \
        -H "Content-Type: application/json" \
        -d '{"courseId":"test-course-1","studentId":"S001"}' | jq .
    sleep 1
done

echo -e "\n完成！请查看各服务日志确认请求分配到不同实例"
