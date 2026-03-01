"""Add new feature tables

Revision ID: add_new_feature_tables
Revises: 1eaa04841c0d
Create Date: 2024-12-30
"""
from alembic import op
import sqlalchemy as sa

# revision identifiers
revision = 'add_new_feature_tables'
down_revision = '1eaa04841c0d'
branch_labels = None
depends_on = None

def upgrade():
    # Create categories table
    op.create_table('categories',
        sa.Column('id', sa.Integer(), primary_key=True),
        sa.Column('name', sa.String(50), nullable=False, unique=True),
        sa.Column('color', sa.String(7), default='#5E72E4'),
        sa.Column('icon', sa.String(50), default='event'),
        sa.Column('created_at', sa.DateTime())
    )
    
    # Create favorites table
    op.create_table('favorites',
        sa.Column('user_id', sa.Integer(), sa.ForeignKey('users.id'), primary_key=True),
        sa.Column('event_id', sa.Integer(), sa.ForeignKey('events.id'), primary_key=True),
        sa.Column('created_at', sa.DateTime())
    )
    
    # Create comments table
    op.create_table('comments',
        sa.Column('id', sa.Integer(), primary_key=True),
        sa.Column('event_id', sa.Integer(), sa.ForeignKey('events.id'), nullable=False),
        sa.Column('user_id', sa.Integer(), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('content', sa.Text(), nullable=False),
        sa.Column('created_at', sa.DateTime())
    )
    
    # Add category_id and recording_url to events table
    try:
        op.add_column('events', sa.Column('category_id', sa.Integer(), sa.ForeignKey('categories.id'), nullable=True))
    except:
        pass
    
    try:
        op.add_column('events', sa.Column('recording_url', sa.String(255), nullable=True))
    except:
        pass

def downgrade():
    op.drop_column('events', 'recording_url')
    op.drop_column('events', 'category_id')
    op.drop_table('comments')
    op.drop_table('favorites')
    op.drop_table('categories')
