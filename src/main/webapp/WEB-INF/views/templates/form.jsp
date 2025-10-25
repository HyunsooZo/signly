<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle} - Signly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <link href="/css/template-builder.css" rel="stylesheet">
</head>
<body>

<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container-fluid">
        <a class="navbar-brand" href="/home">
            <i class="bi bi-file-earmark-text me-2"></i>Signly
        </a>
        <div class="navbar-nav ms-auto">
            <a class="nav-link" href="/home">대시보드</a>
            <a class="nav-link active" href="/templates">템플릿</a>
            <a class="nav-link" href="/contracts">계약서</a>
            <a class="nav-link" href="/profile/signature">서명 관리</a>
            <a class="nav-link" href="/logout">로그아웃</a>
        </div>
    </div>
</nav>

<div class="container-fluid mt-4">
    <div class="row">
        <!-- 왼쪽: 섹션 빌더 -->
        <div class="col-md-8">
            <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h5 class="mb-0">
                        <i class="bi bi-file-earmark-text"></i> 템플릿 편집
                    </h5>
                    <div>
                        <button class="btn btn-sm btn-outline-primary" onclick="previewTemplate()">
                            <i class="bi bi-eye"></i> 미리보기
                        </button>
                        <button class="btn btn-sm btn-success" onclick="saveTemplate()">
                            <i class="bi bi-check-circle"></i> 저장
                        </button>
                    </div>
                </div>
                <div class="card-body">
                    <!-- 제목 입력 -->
                    <div class="mb-3">
                        <label class="form-label fw-bold">템플릿 제목</label>
                        <input type="text"
                               class="form-control form-control-lg"
                               id="templateTitle"
                               placeholder="예: 물품 공급 계약서"
                               value="${template.title}">
                    </div>

                    <hr>

                    <!-- 섹션 목록 -->
                    <div id="sectionList" class="section-list">
                        <!-- 섹션들이 여기에 동적으로 추가됨 -->
                    </div>

                    <!-- 섹션 추가 버튼 -->
                    <div class="text-center mt-3">
                        <div class="btn-group">
                            <button class="btn btn-outline-primary" onclick="addSection('HEADING')">
                                <i class="bi bi-type-h1"></i> 제목
                            </button>
                            <button class="btn btn-outline-primary" onclick="addSection('PARAGRAPH')">
                                <i class="bi bi-text-paragraph"></i> 단락
                            </button>
                            <button class="btn btn-outline-primary" onclick="addSection('TABLE')">
                                <i class="bi bi-table"></i> 표
                            </button>
                            <button class="btn btn-outline-primary" onclick="addSection('DIVIDER')">
                                <i class="bi bi-hr"></i> 구분선
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 오른쪽: 변수 관리 -->
        <div class="col-md-4">
            <div class="card">
                <div class="card-header">
                    <h6 class="mb-0">
                        <i class="bi bi-braces"></i> 변수 관리
                    </h6>
                </div>
                <div class="card-body">
                    <p class="text-muted small">
                        템플릿에서 사용할 변수를 정의하세요.
                        섹션 내용에 <code>{{변수명}}</code> 형식으로 사용할 수 있습니다.
                    </p>

                    <!-- 변수 목록 -->
                    <div id="variableList" class="variable-list mb-3">
                        <!-- 변수들이 여기에 동적으로 추가됨 -->
                    </div>

                    <!-- 변수 추가 버튼 -->
                    <button class="btn btn-sm btn-outline-success w-100" onclick="addVariable()">
                        <i class="bi bi-plus-circle"></i> 변수 추가
                    </button>
                </div>
            </div>

            <!-- 빠른 삽입 -->
            <div class="card mt-3">
                <div class="card-header">
                    <h6 class="mb-0">
                        <i class="bi bi-lightning"></i> 빠른 삽입
                    </h6>
                </div>
                <div class="card-body">
                    <div class="d-grid gap-2">
                        <button class="btn btn-sm btn-outline-secondary text-start" onclick="insertQuickVariable('party1')">
                            <i class="bi bi-person"></i> 갑(당사자)
                        </button>
                        <button class="btn btn-sm btn-outline-secondary text-start" onclick="insertQuickVariable('party2')">
                            <i class="bi bi-person"></i> 을(상대방)
                        </button>
                        <button class="btn btn-sm btn-outline-secondary text-start" onclick="insertQuickVariable('contractDate')">
                            <i class="bi bi-calendar"></i> 계약일
                        </button>
                        <button class="btn btn-sm btn-outline-secondary text-start" onclick="insertQuickVariable('amount')">
                            <i class="bi bi-cash"></i> 금액
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- 미리보기 모달 -->
<div class="modal fade" id="previewModal" tabindex="-1">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">
                    <i class="bi bi-eye"></i> 템플릿 미리보기
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div id="previewContent" class="preview-document">
                    <!-- 미리보기 내용 -->
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="/js/template-builder.js"></script>

<script>
// 현재 섹션과 변수 데이터
let sections = [];
let variables = {};
let sectionIdCounter = 0;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    initializeDefaultVariables();
    renderSections();
    renderVariables();
});

// 기본 변수 초기화
function initializeDefaultVariables() {
    variables = {
        'party1': {
            label: '갑(계약자)',
            type: 'text',
            required: true,
            defaultValue: ''
        },
        'party2': {
            label: '을(계약 상대방)',
            type: 'text',
            required: true,
            defaultValue: ''
        },
        'contractDate': {
            label: '계약 일자',
            type: 'date',
            required: true,
            defaultValue: ''
        },
        'amount': {
            label: '금액',
            type: 'number',
            required: false,
            defaultValue: '0'
        }
    };
}

// 섹션 추가
function addSection(type) {
    const sectionId = 'section_' + (++sectionIdCounter);
    const order = sections.length;

    const section = {
        sectionId: sectionId,
        type: type,
        order: order,
        content: '',
        metadata: getDefaultMetadata(type),
        variables: []
    };

    sections.push(section);
    renderSections();
}

// 섹션 타입별 기본 메타데이터
function getDefaultMetadata(type) {
    switch(type) {
        case 'HEADING':
            return { level: 1, alignment: 'center' };
        case 'PARAGRAPH':
            return { indent: false, alignment: 'left' };
        case 'TABLE':
            return { headers: ['항목', '내용'], rows: [['', '']] };
        case 'DIVIDER':
            return { style: 'solid' };
        default:
            return {};
    }
}

// 섹션 렌더링
function renderSections() {
    const container = document.getElementById('sectionList');
    container.innerHTML = '';

    sections.forEach((section, index) => {
        const sectionHtml = createSectionHtml(section, index);
        container.innerHTML += sectionHtml;
    });
}

// 섹션 HTML 생성
function createSectionHtml(section, index) {
    const typeLabel = getSectionTypeLabel(section.type);
    const typeIcon = getSectionTypeIcon(section.type);

    let html = '<div class="section-card mb-3" data-index="' + index + '">';
    html += '<div class="section-header">';
    html += '<div class="d-flex align-items-center">';
    html += '<i class="bi ' + typeIcon + ' me-2"></i>';
    html += '<span class="badge bg-secondary">' + typeLabel + '</span>';
    html += '<span class="ms-2 text-muted small">#' + (index + 1) + '</span>';
    html += '</div>';
    html += '<div>';
    if (index > 0) {
        html += '<button class="btn btn-sm btn-link" onclick="moveSection(' + index + ', ' + (index - 1) + ')"><i class="bi bi-arrow-up"></i></button>';
    }
    if (index < sections.length - 1) {
        html += '<button class="btn btn-sm btn-link" onclick="moveSection(' + index + ', ' + (index + 1) + ')"><i class="bi bi-arrow-down"></i></button>';
    }
    html += '<button class="btn btn-sm btn-link text-danger" onclick="removeSection(' + index + ')"><i class="bi bi-trash"></i></button>';
    html += '</div>';
    html += '</div>';
    html += '<div class="section-body">';
    html += createSectionEditor(section, index);
    html += '</div>';
    html += '</div>';
    return html;
}

// 섹션 타입별 에디터 생성
function createSectionEditor(section, index) {
    let html = '';
    switch(section.type) {
        case 'HEADING':
            html += '<div class="mb-2">';
            html += '<label class="form-label small">제목 레벨</label>';
            html += '<select class="form-select form-select-sm" onchange="updateMetadata(' + index + ', \'level\', parseInt(this.value))">';
            html += '<option value="1"' + (section.metadata.level === 1 ? ' selected' : '') + '>H1 (가장 큼)</option>';
            html += '<option value="2"' + (section.metadata.level === 2 ? ' selected' : '') + '>H2</option>';
            html += '<option value="3"' + (section.metadata.level === 3 ? ' selected' : '') + '>H3</option>';
            html += '</select>';
            html += '</div>';
            html += '<div class="mb-2">';
            html += '<label class="form-label small">정렬</label>';
            html += '<select class="form-select form-select-sm" onchange="updateMetadata(' + index + ', \'alignment\', this.value)">';
            html += '<option value="left"' + (section.metadata.alignment === 'left' ? ' selected' : '') + '>왼쪽</option>';
            html += '<option value="center"' + (section.metadata.alignment === 'center' ? ' selected' : '') + '>가운데</option>';
            html += '<option value="right"' + (section.metadata.alignment === 'right' ? ' selected' : '') + '>오른쪽</option>';
            html += '</select>';
            html += '</div>';
            html += '<input type="text" class="form-control" placeholder="제목 텍스트 입력 (예: 물품 공급 계약서)"';
            html += ' value="' + section.content + '" onchange="updateContent(' + index + ', this.value)">';
            return html;
        case 'PARAGRAPH':
            html += '<div class="mb-2">';
            html += '<label class="form-label small">정렬</label>';
            html += '<select class="form-select form-select-sm" onchange="updateMetadata(' + index + ', \'alignment\', this.value)">';
            html += '<option value="left"' + (section.metadata.alignment === 'left' ? ' selected' : '') + '>왼쪽</option>';
            html += '<option value="center"' + (section.metadata.alignment === 'center' ? ' selected' : '') + '>가운데</option>';
            html += '<option value="right"' + (section.metadata.alignment === 'right' ? ' selected' : '') + '>오른쪽</option>';
            html += '<option value="justify"' + (section.metadata.alignment === 'justify' ? ' selected' : '') + '>양쪽 정렬</option>';
            html += '</select>';
            html += '</div>';
            html += '<textarea class="form-control" rows="4" placeholder="본문 내용 입력. 변수는 {{변수명}} 형식으로 입력하세요."';
            html += ' onchange="updateContent(' + index + ', this.value)">' + section.content + '</textarea>';
            html += '<div class="form-check mt-2">';
            html += '<input class="form-check-input" type="checkbox" id="indent' + index + '"';
            html += (section.metadata.indent ? ' checked' : '') + ' onchange="updateMetadata(' + index + ', \'indent\', this.checked)">';
            html += '<label class="form-check-label small" for="indent' + index + '">들여쓰기</label>';
            html += '</div>';
            return html;
        case 'TABLE':
            return '<div class="alert alert-info small">표 기능은 향후 업데이트 예정입니다.</div>';
        case 'DIVIDER':
            html += '<div class="mb-2">';
            html += '<label class="form-label small">구분선 스타일</label>';
            html += '<select class="form-select form-select-sm" onchange="updateMetadata(' + index + ', \'style\', this.value)">';
            html += '<option value="solid"' + (section.metadata.style === 'solid' ? ' selected' : '') + '>실선</option>';
            html += '<option value="dashed"' + (section.metadata.style === 'dashed' ? ' selected' : '') + '>점선</option>';
            html += '<option value="dotted"' + (section.metadata.style === 'dotted' ? ' selected' : '') + '>점</option>';
            html += '</select>';
            html += '</div>';
            return html;
        default:
            return '';
    }
}

// 유틸리티 함수들
function getSectionTypeLabel(type) {
    const labels = {
        'HEADING': '제목',
        'PARAGRAPH': '단락',
        'TABLE': '표',
        'DIVIDER': '구분선'
    };
    return labels[type] || type;
}

function getSectionTypeIcon(type) {
    const icons = {
        'HEADING': 'bi-type-h1',
        'PARAGRAPH': 'bi-text-paragraph',
        'TABLE': 'bi-table',
        'DIVIDER': 'bi-hr'
    };
    return icons[type] || 'bi-file';
}

// 섹션 업데이트
function updateContent(index, value) {
    sections[index].content = value;
}

function updateMetadata(index, key, value) {
    sections[index].metadata[key] = value;
}

// 섹션 이동
function moveSection(fromIndex, toIndex) {
    const temp = sections[fromIndex];
    sections[fromIndex] = sections[toIndex];
    sections[toIndex] = temp;

    sections.forEach((s, i) => s.order = i);
    renderSections();
}

// 섹션 삭제
function removeSection(index) {
    if(confirm('이 섹션을 삭제하시겠습니까?')) {
        sections.splice(index, 1);
        sections.forEach((s, i) => s.order = i);
        renderSections();
    }
}

// 변수 렌더링
function renderVariables() {
    const container = document.getElementById('variableList');
    container.innerHTML = '';

    Object.keys(variables).forEach(key => {
        const v = variables[key];
        let html = '<div class="variable-item mb-2">';
        html += '<div class="d-flex justify-content-between align-items-start">';
        html += '<div class="flex-grow-1">';
        html += '<code class="text-primary">{{' + key + '}}</code>';
        html += '<div class="small text-muted">' + v.label + '</div>';
        html += '</div>';
        html += '<button class="btn btn-sm btn-link text-danger p-0" onclick="removeVariable(\'' + key + '\')">';
        html += '<i class="bi bi-x-circle"></i>';
        html += '</button>';
        html += '</div>';
        html += '</div>';
        container.innerHTML += html;
    });
}

// 변수 추가
function addVariable() {
    const name = prompt('변수명을 입력하세요 (영문, 숫자만 가능):');
    if(!name) return;

    if(!/^[a-zA-Z0-9_]+$/.test(name)) {
        alert('변수명은 영문, 숫자, 언더스코어만 사용할 수 있습니다.');
        return;
    }

    if(variables[name]) {
        alert('이미 존재하는 변수명입니다.');
        return;
    }

    const label = prompt('변수 레이블 (한글 설명):');
    if(!label) return;

    variables[name] = {
        label: label,
        type: 'text',
        required: false,
        defaultValue: ''
    };

    renderVariables();
}

// 변수 삭제
function removeVariable(name) {
    if(confirm('변수 {{' + name + '}}을(를) 삭제하시겠습니까?')) {
        delete variables[name];
        renderVariables();
    }
}

// 빠른 변수 삽입
function insertQuickVariable(name) {
    const varText = '{{' + name + '}}';
    navigator.clipboard.writeText(varText).then(() => {
        alert(varText + '이(가) 클립보드에 복사되었습니다!');
    });
}

// 미리보기
function previewTemplate() {
    const previewModal = new bootstrap.Modal(document.getElementById('previewModal'));
    const previewContent = document.getElementById('previewContent');

    let html = '<div class="template-document">';
    sections.forEach(section => {
        html += renderSectionPreview(section);
    });
    html += '</div>';

    previewContent.innerHTML = html;
    previewModal.show();
}

function renderSectionPreview(section) {
    const content = section.content || '[내용 없음]';

    switch(section.type) {
        case 'HEADING':
            const level = section.metadata.level || 1;
            const align = section.metadata.alignment || 'center';
            return '<h' + level + ' style="text-align: ' + align + '">' + content + '</h' + level + '>';
        case 'PARAGRAPH':
            const pAlign = section.metadata.alignment || 'left';
            const indent = section.metadata.indent ? 'text-indent: 2em;' : '';
            return '<p style="text-align: ' + pAlign + '; ' + indent + '">' + content + '</p>';
        case 'DIVIDER':
            const style = section.metadata.style || 'solid';
            return '<hr style="border-style: ' + style + '">';
        default:
            return '';
    }
}

// 저장
function saveTemplate() {
    const title = document.getElementById('templateTitle').value.trim();

    if(!title) {
        alert('템플릿 제목을 입력하세요.');
        return;
    }

    if(sections.length === 0) {
        alert('최소 1개 이상의 섹션을 추가하세요.');
        return;
    }

    const templateData = {
        version: '1.0',
        metadata: {
            title: title,
            description: '',
            createdBy: '',
            variables: variables
        },
        sections: sections
    };

    const jsonContent = JSON.stringify(templateData);

    fetch('/templates', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            title: title,
            sectionsJson: jsonContent
        })
    })
    .then(response => {
        if(response.ok) {
            alert('템플릿이 저장되었습니다.');
            window.location.href = '/templates';
        } else {
            return response.json().then(err => {
                throw new Error(err.message || '저장에 실패했습니다.');
            });
        }
    })
    .catch(error => {
        alert('오류: ' + error.message);
    });
}
</script>

</body>
</html>
