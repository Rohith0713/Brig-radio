from flask import Blueprint, jsonify

bp = Blueprint('podcasts', __name__, url_prefix='/api/podcasts')


@bp.route('', methods=['GET'])
@bp.route('/<path:path>', methods=['GET', 'POST', 'PUT', 'PATCH', 'DELETE'])
def coming_soon(**kwargs):
    """Podcasts feature — coming soon"""
    return jsonify({
        'message': 'Podcasts feature coming soon!',
        'status': 'coming_soon'
    }), 200
